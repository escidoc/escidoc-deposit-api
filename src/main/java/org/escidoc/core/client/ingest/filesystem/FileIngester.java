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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.escidoc.core.client.ingest.AbstractIngester;
import org.escidoc.core.client.ingest.exceptions.ConfigurationException;
import org.escidoc.core.client.ingest.exceptions.IngestException;
import org.escidoc.core.tme.TechnicalMetadataExtractor;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.common.base.Preconditions;

import de.escidoc.core.client.ContainerHandlerClient;
import de.escidoc.core.client.TransportProtocol;
import de.escidoc.core.client.exceptions.EscidocException;
import de.escidoc.core.client.exceptions.InternalClientException;
import de.escidoc.core.client.exceptions.TransportException;
import de.escidoc.core.common.jibx.Marshaller;
import de.escidoc.core.common.jibx.MarshallerFactory;
import de.escidoc.core.resources.common.MetadataRecord;
import de.escidoc.core.resources.common.MetadataRecords;
import de.escidoc.core.resources.common.Result;
import de.escidoc.core.resources.common.TaskParam;
import de.escidoc.core.resources.common.properties.PublicStatus;
import de.escidoc.core.resources.common.reference.ContentModelRef;
import de.escidoc.core.resources.common.reference.ContextRef;
import de.escidoc.core.resources.om.container.Container;
import de.escidoc.core.resources.om.item.Item;
import de.escidoc.core.resources.om.item.StorageType;
import de.escidoc.core.resources.om.item.component.Component;
import de.escidoc.core.resources.om.item.component.ComponentContent;
import de.escidoc.core.resources.om.item.component.ComponentProperties;
import de.escidoc.core.resources.om.item.component.Components;
import edu.harvard.hul.ois.fits.exceptions.FitsException;

/**
 * An Ingester which is able to ingest (load) data from filesystem into an
 * eSciDoc Infrastructure. For every file (or local path to a file) an eSciDoc
 * Item with the file as content is created. If the ID of an existing Container
 * is given as Parent-ID the created Items are added as members to that
 * Container.
 * 
 * @see org.escidoc.core.client.ingest.AbstractIngester
 * @see org.escidoc.core.client.ingest.DefaultIngester
 * 
 * @author Frank Schwichtenberg <Frank.Schwichtenberg@FIZ-Karlsruhe.de>
 * 
 */

public class FileIngester extends AbstractIngester {

    static final Logger LOG = LoggerFactory.getLogger(FileIngester.class
        .getName());

    private List<File> files;

    private List<String> itemIDs;

    private String parentId;

    private boolean forceCreate = false;

    /**
     * @param eSciDocInfrastructureBaseUrl
     *            The HTTP URL of an eSciDoc Infrastructure.
     * @param userHandle
     *            The authorization handle of a logged in user.
     * @param parentId
     *            The ID of the Container in the eSciDoc Infrastructure the
     *            files should be added to. If null the files are created
     *            without a parent.
     * 
     * @throws NullPointerException
     *             If one of the parameters is null.
     */
    public FileIngester(String eSciDocInfrastructureBaseUrl, String userHandle,
        String parentId) {
        super();
        init(eSciDocInfrastructureBaseUrl, userHandle, parentId);
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
    private void init(
        String eSciDocInfrastructureBaseUrl, String userHandle, String parentId) {
        if (eSciDocInfrastructureBaseUrl == null) {
            throw new NullPointerException(
                "Param eSciDocInfrastructureBaseUrl must not be null.");
        }
        if (userHandle == null) {
            throw new NullPointerException("Param userHandle must not be null.");
        }

        try {
            seteSciDocInfrastructureBaseUrl(new URL(
                eSciDocInfrastructureBaseUrl));
            setUserHandle(userHandle);
        }
        catch (ConfigurationException e) {
            LOG
                .error(
                    "Can not set infrastructure URL or user handle creating new Ingester.",
                    e);
        }
        catch (MalformedURLException mfue) {
            LOG.error("Invalid infrastructure URL.", mfue);
        }
        this.parentId = parentId;
        files = new Vector<File>();

        loadConfiguration();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.escidoc.core.client.ingest.AbstractIngester#ingestHook()
     * 
     * @throws NullPointerException If the file of this Node is
     * <code>null</code>.
     */
    @Override
    protected final void ingestHook() throws ConfigurationException,
        IngestException {

        if (files.isEmpty()) {
            throw new ConfigurationException("No files to ingest.");
        }
        itemIDs = new Vector<String>();
        if (this.ingestProgressListener != null) {
            this.ingestProgressListener.setSum(files.size());
        }

        for (File file : files) {
            try {
                // FIXME exception handling
                ingestItem(file);
                if (this.ingestProgressListener != null) {
                    this.ingestProgressListener.incrementIngested();
                }
            }
            catch (EscidocException e) {
                String msg = "Item failed " + file;
                LOG.error("Item failed :" + msg, e);
                LOG.debug("Ingest failed, filename:" + file);
                if (this.ingestProgressListener != null) {
                    LOG.debug("Ingest failed, itemNumber:"
                        + this.ingestProgressListener.getIngested() + 1, e);
                }
                // errorNode = n;
                // isError=true;
            }
            catch (InternalClientException e) {
                String msg = "Error in eSciDoc Client.";
                LOG.error(msg, e);
                // errorNode = n;
                // isError = true;

            }
            catch (TransportException e) {
                // FIXME reason for Transport Exception?
                String msg = "Communication error.";
                LOG.error(msg, e);
                // errorNode = n;
                // isError = true;
            }
        }

        if (parentId != null) {
            setChildren();
        }
    }

    private void setChildren() throws IngestException {
        try {
            ContainerHandlerClient chc =
                new ContainerHandlerClient(geteSciDocInfrastructureBaseUrl());
            chc.setHandle(getUserHandle());
            Container parent = chc.retrieve(parentId);
            DateTime parentLastModificationDate =
                parent.getLastModificationDate();
            TaskParam taskParam = new TaskParam();

            for (String itemId : itemIDs) {
                LOG.debug("Adding to parent[" + parentId + "]: " + itemId);
                taskParam.setLastModificationDate(parentLastModificationDate);
                taskParam.addResourceRef(itemId);
            }
            chc.addMembers(parentId, taskParam);
            LOG.debug("...added.");
        }
        catch (Exception e) {
            throw new IngestException(e);
        }
    }

    /**
     * Ingests an Item from a Node that represents a file. Overwrite this method
     * in order to change the implementation of creating an Item from a file
     * node.
     * 
     * @param contentFile
     *            A node representing a file.
     * 
     * @throws InternalClientException
     *             If an internal error in the eSciDoc Client Library occurs.
     * @throws EscidocException
     *             If an error occurs in the underlying eSciDoc Infrastructure.
     * @throws TransportException
     *             If an transport error in the eSciDoc Client Library occurs.
     */
    protected void ingestItem(File contentFile) throws EscidocException,
        InternalClientException, TransportException {
        Item item = new Item();

        // properties
        item.getProperties().setContentModel(
            new ContentModelRef(getItemContentModel()));
        item.getProperties().setContext(new ContextRef(getContext()));
        if (getInitialLifecycleStatus().equals(PublicStatus.RELEASED)) {
            item.getProperties().setPid("no:pid/test");
        }
        item.getProperties().setPublicStatus(getInitialLifecycleStatus());
        item.getProperties().setPublicStatusComment(
            "Item ingested via Ingest Client API");

        // add generated descriptive matadata
        item.setMetadataRecords(new MetadataRecords());
        item.getMetadataRecords().add(createOaiDcMetadata(contentFile));

        // content
        Component component = new Component();
        component.setProperties(new ComponentProperties());
        component.getProperties().setContentCategory(getContentCategory());
        component.getProperties().setValidStatus(getValidStatus());
        component.getProperties().setVisibility(getVisibility());
        component.getProperties().setMimeType(getMimeType());

        // add generated technical metadata
        MetadataRecord contentMd = createContentMetadata(contentFile);
        if (contentMd != null && contentMd.getContent() != null) {
            component.setMetadataRecords(new MetadataRecords());
            component.getMetadataRecords().add(contentMd);
        }

        ComponentContent content = new ComponentContent();
        content.setStorage(StorageType.INTERNAL_MANAGED);

        URL stagingFile = getStagingHandlerClient().upload(contentFile);

        content.setXLinkHref(stagingFile.toString());
        component.setContent(content);
        item.setComponents(new Components());
        item.getComponents().add(component);

        // create or ingest? different rights are needed. Reason is BW-eLab
        // depoit of experiment data.

        String itemId = null;
        if (this.forceCreate) {
            // create
            Item createdItem = getItemHandlerClient().create(item);
            itemId = createdItem.getObjid();
        }
        else {
            // ingest
            MarshallerFactory mf =
                MarshallerFactory.getInstance(TransportProtocol.REST);
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
            itemId = r.getFirst().getTextContent();
        }

        itemIDs.add(itemId);
    }

    /**
     * Creates OAI Dublic Core metadata for a filesystem node which represents a
     * file or directory. For files this is called from
     * {@link FileIngester#ingestItem(Node)} and for directories from
     * {@link FileIngester#ingestContainer(Node)}. Overwrite this method in
     * order to implement specialized creation of Item and Container metadata.
     * 
     * @param n
     *            The node representing a file or directory.
     * @return A metadatarecord object containing OAI DC metadata.
     */
    protected MetadataRecord createOaiDcMetadata(File n) {
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

        Element oaiDc =
            dcContentDocument.createElementNS(
                "http://www.openarchives.org/OAI/2.0/oai_dc/", "oai_dc:dc");

        Element dcTitle =
            dcContentDocument.createElementNS(
                "http://purl.org/dc/elements/1.1/", "title");
        dcTitle.setTextContent(n.getName());
        oaiDc.appendChild(dcTitle);

        dcContentDocument.appendChild(oaiDc);

        MetadataRecord dc = new MetadataRecord("escidoc");
        dc.setContent(dcContentDocument.getDocumentElement());
        return dc;
    }

    /**
     * Creates technical metadata for a files. This is called from
     * {@link FileIngester#ingestItem(Node)} and the result (if not
     * <code>null</code>) is stored as metadata in the componenent holding the
     * file as content inside the ingested Item. Overwrite this method in order
     * to implement specialized creation of technical metadata.
     * 
     * @param file
     *            The file to create technical metadata for.
     * @return A metadatarecord object containing technical metadata.
     */
    protected MetadataRecord createContentMetadata(File file) {
        Preconditions.checkNotNull(file, "file is null: %s", file);

        MetadataRecord metadata = new MetadataRecord("escidoc");

        // FIXME no longer optional
        if (getFitsHome() != null) {

            try {
                metadata.setContent(new TechnicalMetadataExtractor(
                    getFitsHome()).extract(file));
            }
            catch (FitsException e) {
                LOG.warn(
                    "Fail to extract technical metadata " + e.getMessage(), e);
            }
            catch (SAXException e) {
                LOG.warn(
                    "Fail to extract technical metadata " + e.getMessage(), e);
            }
            catch (IOException e) {
                LOG.warn(
                    "Fail to extract technical metadata " + e.getMessage(), e);
            }
            catch (ParserConfigurationException e) {
                LOG.warn(
                    "Fail to extract technical metadata " + e.getMessage(), e);
            }

        }
        return metadata;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.escidoc.core.client.ingest.AbstractIngester#checkConfiguration()
     */
    @Override
    public void checkConfiguration() throws ConfigurationException {
        super.checkConfiguration();

        if (files.isEmpty()) {
            throw new ConfigurationException("Files must be set.");
        }
    }

    public void setForceCreate(boolean forceCreate) {
        this.forceCreate = forceCreate;
    }

    public void addFile(File f) {
        files.add(f);
    }

    public void addFile(String f) {
        this.addFile(new File(f));
    }

    public List<String> getItemIDs() {
        return new Vector<String>(itemIDs);
    }
}
