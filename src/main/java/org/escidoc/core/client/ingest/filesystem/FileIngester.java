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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.escidoc.core.client.ingest.AbstractIngester;
import org.escidoc.core.client.ingest.exceptions.ConfigurationException;
import org.escidoc.core.client.ingest.exceptions.IngestException;
import org.escidoc.core.client.ingest.util.IngestConfiguration;
import org.escidoc.core.client.ingest.ws.WebService;
import org.escidoc.core.client.ingest.ws.exceptions.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.escidoc.core.client.TransportProtocol;
import de.escidoc.core.client.exceptions.EscidocException;
import de.escidoc.core.client.exceptions.InternalClientException;
import de.escidoc.core.client.exceptions.TransportException;
import de.escidoc.core.common.jibx.Marshaller;
import de.escidoc.core.common.jibx.MarshallerFactory;
import de.escidoc.core.resources.common.MetadataRecord;
import de.escidoc.core.resources.common.MetadataRecords;
import de.escidoc.core.resources.common.Result;
import de.escidoc.core.resources.common.reference.ContentModelRef;
import de.escidoc.core.resources.common.reference.ContextRef;
import de.escidoc.core.resources.om.item.Item;
import de.escidoc.core.resources.om.item.StorageType;
import de.escidoc.core.resources.om.item.component.Component;
import de.escidoc.core.resources.om.item.component.ComponentContent;
import de.escidoc.core.resources.om.item.component.ComponentProperties;
import de.escidoc.core.resources.om.item.component.Components;

/**
 * An Ingester which is able to ingest (load) data from filesystem into an
 * eSciDoc Infrastructure. For every file an eSciDoc Item with the file as
 * content is created and for every a container is created which has
 * subdirectories and included files as members.
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

    public void addFile(File f) {
	files.add(f);
    }

    public void addFile(String f) {
	this.addFile(new File(f));
    }

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
    private void init(String eSciDocInfrastructureBaseUrl, String userHandle,
	    String parentId) {
	if (eSciDocInfrastructureBaseUrl == null) {
	    throw new NullPointerException(
		    "Param eSciDocInfrastructureBaseUrl must not be null.");
	}
	if (userHandle == null) {
	    throw new NullPointerException("Param userHandle must not be null.");
	}

	try {
	    this.seteSciDocInfrastructureBaseUrl(new URL(
		    eSciDocInfrastructureBaseUrl));
	    this.setUserHandle(userHandle);
	} catch (ConfigurationException e) {
	    LOG.error(
		    "Can not set infrastructure URL or user handle creating new Ingester.",
		    e);
	} catch (MalformedURLException mfue) {
	    LOG.error("Invalid infrastructure URL.", mfue);
	}
	this.parentId = parentId;
	this.files = new Vector<File>();

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

	if (this.files.isEmpty()) {
	    throw new ConfigurationException("No files to ingest.");
	}
	this.itemIDs = new Vector<String>();
	Count c = new Count(this.ingestProgressListener);

	for (File file : this.files) {
	    try {
		ingestItem(file);
		c.increment();
	    } catch (EscidocException e) {
		String msg = "Item failed " + file;
		LOG.error("Item failed :" + msg, e);
		LOG.debug("Ingest failed, filename:" + file);
		LOG.debug("Ingest failed, itemNumber:" + c.getValue(), e);
		// errorNode = n;
		// isError=true;
	    } catch (InternalClientException e) {
		String msg = "Error in eSciDoc Client.";
		LOG.error(msg, e);
		// errorNode = n;
		// isError = true;

	    } catch (TransportException e) {
		// FIXME reason for Transport Exception?
		String msg = "Communication error.";
		LOG.error(msg, e);
		// errorNode = n;
		// isError = true;
	    }
	}

	if (this.parentId != null) {
	    // FIXME
	    for (String itemId : this.itemIDs) {
		System.out.println("Must be added to parent: " + itemId
			+ " to " + this.parentId);
		LOG.error("Not impelemented");
	    }
	}
    }

    /**
     * Ingests an Item from a Node that represents a file. Overwrite this method
     * in order to change the implementation of creating an Item from a file
     * node.
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
    protected void ingestItem(File n) throws EscidocException,
	    InternalClientException, TransportException {
	Item item = new Item();

	// properties
	item.getProperties().setContentModel(
		new ContentModelRef(this.getItemContentModel()));
	item.getProperties().setContext(new ContextRef(this.getContext()));
	if (this.getInitialLifecycleStatus().equals("released")) {
	    item.getProperties().setPid("no:pid/test");
	}
	item.getProperties().setPublicStatus(this.getInitialLifecycleStatus());
	item.getProperties().setPublicStatusComment(
		"Item ingested via Ingest Client API");

	item.setMetadataRecords(new MetadataRecords());
	item.getMetadataRecords().add(createOaiDcMetadata(n));

	// content
	Component component = new Component();
	component.setProperties(new ComponentProperties());
	component.getProperties().setContentCategory(this.getContentCategory());
	component.getProperties().setValidStatus(this.getValidStatus());
	component.getProperties().setVisibility(this.getVisibility());
	component.getProperties().setMimeType(this.getMimeType());

	MetadataRecord contentMd = createContentMetadata(n);
	if (contentMd != null && contentMd.getContent() != null) {
	    component.setMetadataRecords(new MetadataRecords());
	    component.getMetadataRecords().add(contentMd);
	}

	ComponentContent content = new ComponentContent();
	content.setStorage(StorageType.INTERNAL_MANAGED);

	URL stagingFile = this.getStagingHandlerClient().upload(n);

	content.setXLinkHref(stagingFile.toString());
	component.setContent(content);
	item.setComponents(new Components());
	item.getComponents().add(component);

	// ingest
	MarshallerFactory mf = MarshallerFactory
		.getInstance(TransportProtocol.REST);
	Marshaller<Item> im = mf.getMarshaller(Item.class);
	String itemXml = im.marshalDocument(item);
	String result;
	try {
	    result = this.getIngestHandlerClient().ingest(itemXml);
	} catch (EscidocException e) {
	    System.out.println(itemXml);
	    throw e;
	}
	// store result
	System.out.println("result[" + result + "]");
	Marshaller<Result> rm = mf.getMarshaller(Result.class);
	Result r = rm.unmarshalDocument(result);
	String itemId = r.getFirst().getTextContent();
	// Item created = this.getItemHandlerClient().retrieve(itemId);
	this.itemIDs.add(itemId);
	// n.getResource().setIdentifier(itemId);
	// n.getResource().setObjectType("item");
	// n.getResource().setTitle(n.getName());
	// n.getResource().setHref("/ir/item/" + itemId);
	// n.setIsIngested(true);

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
	} catch (ParserConfigurationException e) {
	    // FIXME what kind of exception
	    throw new RuntimeException(e);
	}
	Document dcContentDocument = db.newDocument();

	Element oaiDc = dcContentDocument.createElementNS(
		"http://www.openarchives.org/OAI/2.0/oai_dc/", "oai_dc:dc");

	Element dcTitle = dcContentDocument.createElementNS(
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

	MetadataRecord metadata = new MetadataRecord("escidoc");

	try {
	    Collection<WebService> webservices = IngestConfiguration
		    .getContentWebservices();
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
	} catch (MalformedURLException e) {
	    // FIXME
	    throw new RuntimeException(e);
	} catch (ExecutionException e) {
	    // FIXME
	    throw new RuntimeException(e);
	} catch (ConfigurationException e) {
	    // FIXME
	    throw new RuntimeException(e);
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

	if (this.files.isEmpty()) {
	    throw new ConfigurationException("Files must be set.");
	}
    }

}
