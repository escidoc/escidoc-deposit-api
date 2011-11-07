/**
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at license/ESCIDOC.LICENSE
 * or https://www.escidoc.org/license/ESCIDOC.LICENSE .
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at license/ESCIDOC.LICENSE.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 *
 *
 * Copyright 2011 Fachinformationszentrum Karlsruhe Gesellschaft
 * fuer wissenschaftlich-technische Information mbH and Max-Planck-
 * Gesellschaft zur Foerderung der Wissenschaft e.V.
 * All rights reserved.  Use is subject to license terms.
 */
package org.escidoc.core.client.ingest.filesystem;

import org.escidoc.core.client.ingest.AbstractIngester;
import org.escidoc.core.client.ingest.exceptions.AlreadyIngestedException;
import org.escidoc.core.client.ingest.exceptions.ConfigurationException;
import org.escidoc.core.client.ingest.exceptions.IngestException;
import org.escidoc.core.client.ingest.ws.WebService;
import org.escidoc.core.client.ingest.ws.exceptions.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import de.escidoc.core.client.TransportProtocol;
import de.escidoc.core.client.exceptions.EscidocException;
import de.escidoc.core.client.exceptions.InternalClientException;
import de.escidoc.core.client.exceptions.TransportException;
import de.escidoc.core.common.jibx.Marshaller;
import de.escidoc.core.common.jibx.MarshallerFactory;
import de.escidoc.core.resources.common.MetadataRecord;
import de.escidoc.core.resources.common.MetadataRecords;
import de.escidoc.core.resources.common.Result;
import de.escidoc.core.resources.common.properties.PublicStatus;
import de.escidoc.core.resources.common.reference.ContentModelRef;
import de.escidoc.core.resources.common.reference.ContextRef;
import de.escidoc.core.resources.common.structmap.ContainerMemberRef;
import de.escidoc.core.resources.common.structmap.ItemMemberRef;
import de.escidoc.core.resources.common.structmap.StructMap;
import de.escidoc.core.resources.om.container.Container;
import de.escidoc.core.resources.om.item.Item;
import de.escidoc.core.resources.om.item.StorageType;
import de.escidoc.core.resources.om.item.component.Component;
import de.escidoc.core.resources.om.item.component.ComponentContent;
import de.escidoc.core.resources.om.item.component.ComponentProperties;
import de.escidoc.core.resources.om.item.component.Components;

/**
 * An Ingester which is able to ingest (load) data from filesystem into an eSciDoc Infrastructure. For every file an
 * eSciDoc Item with the file as content is created and for every a container is created which has subdirectories and
 * included files as members.
 * 
 * @see org.escidoc.core.client.ingest.AbstractIngester
 * @see org.escidoc.core.client.ingest.DefaultIngester
 * 
 * @author Frank Schwichtenberg <Frank.Schwichtenberg@FIZ-Karlsruhe.de>
 * 
 */

public class DirectoryIngester extends AbstractIngester {

    static final Logger LOG = LoggerFactory.getLogger(DirectoryIngester.class.getName());

    private File directory;

    private Node root;

    private boolean saveDirectoryAsContainer = true;

    /**
     * @param eSciDocInfrastructureBaseUrl
     *            The HTTP URL of an eSciDoc Infrastructure.
     * @param userHandle
     *            The authorization handle of a logged in user.
     * @param directory
     *            The path to the directory that should be ingested.
     * 
     * @throws NullPointerException
     *             If one of the parameters is null.
     */
    public DirectoryIngester(String eSciDocInfrastructureBaseUrl, String userHandle, String directory) {
        super();
        init(eSciDocInfrastructureBaseUrl, userHandle, new File(directory));
    }

    /**
     * @param eSciDocInfrastructureBaseUrl
     *            The HTTP URL of an eSciDoc Infrastructure.
     * @param userHandle
     *            The authorization handle of a logged in user.
     * @param directory
     *            The path to the directory that should be ingested.
     * 
     * @throws NullPointerException
     *             If one of the parameters is null.
     */
    public DirectoryIngester(String eSciDocInfrastructureBaseUrl, String userHandle, File directory) {
        super();
        init(eSciDocInfrastructureBaseUrl, userHandle, directory);
    }

    /**
     * @param eSciDocInfrastructureBaseUrl
     *            The HTTP URL of an eSciDoc Infrastructure.
     * @param userHandle
     *            The authorization handle of a logged in user.
     * @param directory
     *            The path to the directory that should be ingested.
     * 
     * @throws NullPointerException
     *             If one of the parameters is null.
     */
    private void init(String eSciDocInfrastructureBaseUrl, String userHandle, File directory) {
        if (eSciDocInfrastructureBaseUrl == null) {
            throw new NullPointerException("Param eSciDocInfrastructureBaseUrl must not be null.");
        }
        if (userHandle == null) {
            throw new NullPointerException("Param userHandle must not be null.");
        }

        try {
            seteSciDocInfrastructureBaseUrl(new URL(eSciDocInfrastructureBaseUrl));
            setUserHandle(userHandle);
        }
        catch (ConfigurationException e) {
            LOG.error("Can not set infrastructure URL or user handle creating new Ingester.", e);
        }
        catch (MalformedURLException mfue) {
            LOG.error("Invalid infrastructure URL.", mfue);
        }
        this.directory = directory;

        loadConfiguration();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.escidoc.core.client.ingest.AbstractIngester#ingestHook()
     * 
     * @throws NullPointerException If the file of this Node is <code>null</code>.
     */
    @Override
    protected final void ingestHook() throws ConfigurationException, IngestException {

        if (directory == null) {
            throw new NullPointerException("Param directory must not be null.");
        }

        try {
            List<Node> items = new Vector<Node>();
            Count c = new Count(ingestProgressListener);
            root = new Node(items, c);
            root.setFile(directory);
            root.dive();

            Iterator<Node> nodeIt = items.iterator();

            while (nodeIt.hasNext() && !isCanceled) {
                Node n;
                n = nodeIt.next();
                LOG.debug("Ingesting " + n.getFile().getPath());
                try {
                    ingest(n);
                }
                catch (EscidocException e) {
                    LOG.debug("Ingest failed, filename:" + n.getFile());
                    LOG.debug("Ingest failed, itemNumber:" + c.getValue(), e);
                }
                catch (InternalClientException e) {
                    String msg = "Error in eSciDoc Client.";
                    LOG.error(msg, e);
                }
                catch (TransportException e) {
                    // FIXME reason for Transport Exception?
                    String msg = "Communication error.";
                    LOG.error(msg, e);
                }
                catch (AlreadyIngestedException e) {
                    String msg = "An already ingested entity was tried to be ingested.";
                    LOG.error(msg, e);
                }
                catch (FileNotFoundException e) {
                    String msg = "File to be ingested can not be found.";
                    LOG.error(msg, e);
                }
            }
        }
        catch (FileNotFoundException e) {
            String msg = "File to be ingested can not be found.";
            LOG.error(msg, e);
            throw new IngestException(msg, e);
        }

    }

    /**
     * @param n
     *            A node representing a file or directory.
     * 
     * @throws AlreadyIngestedException
     *             If the Node is already ingested.
     * @throws FileNotFoundException
     *             If the file of the Node does not exist.
     * @throws InternalClientException
     *             If an internal error in the eSciDoc Client Library occurs.
     * @throws EscidocException
     *             If an error occurs in the underlying eSciDoc Infrastructure.
     * @throws TransportException
     *             If an transport error in the eSciDoc Client Library occurs.
     * @throws NullPointerException
     *             If the file or the leaves of the Node are <code>null</code>.
     */
    private void ingest(Node n) throws AlreadyIngestedException, FileNotFoundException, InternalClientException,
        EscidocException, TransportException {

        // check if cancel

        if (n.getFile() == null) {
            throw new NullPointerException("A file must be set before diving.");
        }
        if (!n.getFile().exists()) {
            throw new FileNotFoundException("The given file does not exist. " + n.getFile().getPath());
        }

        if (n.isIngested()) {
            throw new AlreadyIngestedException();
        }

        if (n.getFile().isDirectory()) {
            try {
                ingestContainer(n);
            }
            catch (EscidocException e) {
                String msg = "Container failed " + n.getFile();
                System.out.println(msg);
                LOG.error(msg, e);
                throw e;
            }
        }
        else {
            try {
                ingestItem(n);
            }
            catch (EscidocException e) {
                String msg = "Item failed " + n.getFile();
                System.out.println(msg);
                System.out.println(e);
                LOG.error("Item failed :" + msg, e);
                throw e;
            }
        }
        if (ingestProgressListener != null) {
            ingestProgressListener.incrementIngested();
        }

        // if all children of parent are already ingested, ingest parent
        // if no parent, ready
        Node parent = n.getParent();
        if (parent != null) {
            // assume thats the case
            boolean allChildrenIngested = true;
            // and try to find child which is not ingested
            Iterator<Node> childIt = parent.getChildren().iterator();
            while (childIt.hasNext() && !isCanceled) {
                Node child = childIt.next();
                if (!child.isIngested()) {
                    allChildrenIngested = false;
                    break;
                }
            }
            if (allChildrenIngested) {
                this.ingest(parent);
            }
        }
    }

    /**
     * Ingests a Container from a Node that represents a directory. Overwrite this method in order to change the
     * implementation of creating a Container from a directory node.
     * 
     * @param n
     *            A node representing a directory.
     * 
     * @throws InternalClientException
     *             If an internal error in the eSciDoc Client Library occurs.
     * @throws EscidocException
     *             If an error occurs in the underlying eSciDoc Infrastructure.
     * @throws TransportException
     *             If an transport error in the eSciDoc Client Library occurs.
     */
    protected void ingestContainer(Node n) throws InternalClientException, EscidocException, TransportException {
        // TODO assert node represents a directory
        Container container = new Container();

        // properties
        container.getProperties().setContentModel(new ContentModelRef(getContainerContentModel()));
        container.getProperties().setContext(new ContextRef(getContext()));
        if (getInitialLifecycleStatus().equals(PublicStatus.RELEASED)) {
            container.getProperties().setPid("no:pid/test");
        }
        container.getProperties().setPublicStatus(PublicStatus.OPENED);// this.getInitialLifecycleStatus());
        container.getProperties().setPublicStatusComment("Container ingested via Ingest Client API");

        container.setMetadataRecords(new MetadataRecords());
        container.getMetadataRecords().add(createOaiDcMetadata(n));

        // struct-map
        container.setStructMap(new StructMap());
        Iterator<Node> childIt = n.getChildren().iterator();
        List<ContainerMemberRef> containerRefs = new Vector<ContainerMemberRef>();
        while (childIt.hasNext() && !isCanceled) {
            Node child = childIt.next();
            if (child.getFile().isDirectory()) {
                // containers must be added after adding items
                containerRefs.add(new ContainerMemberRef(child.getResource().getIdentifier()));
            }
            else {
                container.getStructMap().add(new ItemMemberRef(child.getResource().getIdentifier()));
            }
        }
        // add containers
        Iterator<ContainerMemberRef> containerRefIt = containerRefs.iterator();
        while (containerRefIt.hasNext()) {
            container.getStructMap().add(containerRefIt.next());
        }

        // ingest
        MarshallerFactory mf = MarshallerFactory.getInstance(TransportProtocol.REST);
        Marshaller<Container> cm = mf.getMarshaller(Container.class);
        String containerXml = cm.marshalDocument(container);
        String resultXml;
        try {
            resultXml = getIngestHandlerClient().ingest(containerXml);
        }
        catch (EscidocException e) {
            LOG.error(containerXml);
            throw e;
        }

        // store result
        LOG.info("result[" + resultXml + "]");
        Marshaller<Result> rm = mf.getMarshaller(Result.class);
        Result result = rm.unmarshalDocument(resultXml);
        String containerId = result.getFirst().getTextContent();
        // Container created =
        // this.getContainerHandlerClient().retrieve(containerId);
        n.getResource().setIdentifier(containerId);
        n.getResource().setObjectType("container");
        n.getResource().setTitle(n.getFile().getName());
        n.getResource().setHref("/ir/container/" + containerId);
        n.setIsIngested(true);
    }

    /**
     * Ingests an Item from a Node that represents a file. Overwrite this method in order to change the implementation
     * of creating an Item from a file node.
     * 
     * @param n
     *            A node representing a file.
     * 
     * @throws InternalClientException
     *             If an internal error in the eSciDoc Client Library occurs.
     * @throws EscidocException
     *             If an error occurs in the underlying eSciDoc Infrastructure.
     * @throws TransportException
     *             If an transport error in the eSciDoc Client Library occurs.
     */
    protected void ingestItem(Node n) throws EscidocException, InternalClientException, TransportException {
        // TODO assert node represents a file
        Item item = new Item();

        // properties
        item.getProperties().setContentModel(new ContentModelRef(getItemContentModel()));
        item.getProperties().setContext(new ContextRef(getContext()));
        if (getInitialLifecycleStatus().equals(PublicStatus.RELEASED)) {
            item.getProperties().setPid("no:pid/test");
        }
        item.getProperties().setPublicStatus(getInitialLifecycleStatus());
        item.getProperties().setPublicStatusComment("Item ingested via Ingest Client API");

        item.setMetadataRecords(new MetadataRecords());
        item.getMetadataRecords().add(createOaiDcMetadata(n));

        // content
        Component component = new Component();
        component.setProperties(new ComponentProperties());
        component.getProperties().setContentCategory(getContentCategory());
        component.getProperties().setValidStatus(getValidStatus());
        component.getProperties().setVisibility(getVisibility());
        component.getProperties().setMimeType(getMimeType());

        MetadataRecord contentMd = createContentMetadata(n.getFile());
        if (contentMd != null && contentMd.getContent() != null) {
            component.setMetadataRecords(new MetadataRecords());
            component.getMetadataRecords().add(contentMd);
        }

        ComponentContent content = new ComponentContent();
        content.setStorage(StorageType.INTERNAL_MANAGED);

        URL stagingFile = getStagingHandlerClient().upload(n.getFile());

        content.setXLinkHref(stagingFile.toString());
        component.setContent(content);
        item.setComponents(new Components());
        item.getComponents().add(component);

        // ingest
        MarshallerFactory mf = MarshallerFactory.getInstance(TransportProtocol.REST);
        Marshaller<Item> im = mf.getMarshaller(Item.class);
        String itemXml = im.marshalDocument(item);
        String result;
        try {
            result = getIngestHandlerClient().ingest(itemXml);
        }
        catch (EscidocException e) {
            System.out.println(itemXml);
            throw e;
        }
        // store result
        System.out.println("result[" + result + "]");
        Marshaller<Result> rm = mf.getMarshaller(Result.class);
        Result r = rm.unmarshalDocument(result);
        String itemId = r.getFirst().getTextContent();
        // Item created = this.getItemHandlerClient().retrieve(itemId);
        n.getResource().setIdentifier(itemId);
        n.getResource().setObjectType("item");
        n.getResource().setTitle(n.getFile().getName());
        n.getResource().setHref("/ir/item/" + itemId);
        n.setIsIngested(true);
    }

    /**
     * Creates OAI Dublic Core metadata for a filesystem node which represents a file or directory. For files this is
     * called from {@link DirectoryIngester#ingestItem(Node)} and for directories from
     * {@link DirectoryIngester#ingestContainer(Node)}. Overwrite this method in order to implement specialized creation
     * of Item and Container metadata.
     * 
     * @param n
     *            The node representing a file or directory.
     * @return A metadatarecord object containing OAI DC metadata.
     */
    protected MetadataRecord createOaiDcMetadata(Node n) {
        // dc metadata
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
        }
        catch (ParserConfigurationException e) {
            // FIXME what kind of exception
            throw new RuntimeException(e);
        }
        Document dcContentDocument = db.newDocument();

        Element oaiDc = dcContentDocument.createElementNS("http://www.openarchives.org/OAI/2.0/oai_dc/", "oai_dc:dc");

        Element dcTitle = dcContentDocument.createElementNS("http://purl.org/dc/elements/1.1/", "title");
        dcTitle.setTextContent(n.getFile().getName());
        oaiDc.appendChild(dcTitle);

        dcContentDocument.appendChild(oaiDc);

        MetadataRecord dc = new MetadataRecord("escidoc");
        dc.setContent(dcContentDocument.getDocumentElement());
        return dc;
    }

    /**
     * Creates technical metadata for a files. This is called from {@link DirectoryIngester#ingestItem(Node)} and the
     * result (if not <code>null</code>) is stored as metadata in the componenent holding the file as content inside the
     * ingested Item. Overwrite this method in order to implement specialized creation of technical metadata.
     * 
     * @param file
     *            The file to create technical metadata for.
     * @return A metadatarecord object containing technical metadata.
     */
    protected MetadataRecord createContentMetadata(File file) {

        MetadataRecord metadata = new MetadataRecord("escidoc");

        try {
            Collection<WebService> webservices = new ArrayList<WebService>();

            // .getContentWebservices();
            Iterator<WebService> it = webservices.iterator();
            while (it.hasNext() && !isCanceled) {
                WebService ws = it.next();
                ws.addParams(file);
                LOG.error("calling webservice for " + file.getPath());
                Object result = ws.call();
                if (result instanceof Document) {
                    Document doc = (Document) result;
                    metadata.setContent(doc.getDocumentElement());
                }
            }
        }
        catch (MalformedURLException e) {
            // FIXME
            throw new RuntimeException(e);
        }
        catch (ExecutionException e) {
            // FIXME
            throw new RuntimeException(e);
        }

        return metadata;

    }

    public String getDirectory() {
        return directory.getPath();
    }

    public void setDirectory(String directory) {
        this.directory = new File(directory);
    }

    public Node getRoot() {
        return root;
    }

    public void setDirectory(File directory) {
        this.directory = directory;
    }

    // @Override
    /**
     * Set if a directory should be stored as Container in the underlying eSciDoc Infrastructure.
     * 
     * @return <code>true</code> if a directory should stored as Container in the underlying eSciDoc Infrastructure.
     *         <code>false</code> otherwise.
     */
    public boolean getSaveDirectoryAsContainer() {
        return saveDirectoryAsContainer;
    }

    /**
     * Set if a directory should be stored as Container in the underlying eSciDoc Infrastructure.
     * 
     * @param saveDirectoryAsContainer
     *            A boolean indication if a directory should be stored as Container in the underlying eSciDoc
     *            Infrastructure.
     */
    // @Override
    public void setSaveDirectoryAsContainer(boolean saveDirectoryAsContainer) {
        this.saveDirectoryAsContainer = saveDirectoryAsContainer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.escidoc.core.client.ingest.AbstractIngester#checkConfiguration()
     */
    @Override
    public void checkConfiguration() throws ConfigurationException {
        super.checkConfiguration();

        if (directory == null) {
            throw new ConfigurationException("Directory must be set.");
        }
    }

}
