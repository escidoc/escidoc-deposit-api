package org.escidoc.core.client.ingest.byname;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.escidoc.core.client.ingest.AbstractIngester;
import org.escidoc.core.client.ingest.entities.ResourceEntry;
import org.escidoc.core.client.ingest.exceptions.ConfigurationException;
import org.escidoc.core.client.ingest.exceptions.IngestException;
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
import de.escidoc.core.resources.Resource;
import de.escidoc.core.resources.ResourceType;
import de.escidoc.core.resources.cmm.ContentModel;
import de.escidoc.core.resources.cmm.ContentModelProperties;
import de.escidoc.core.resources.common.MetadataRecord;
import de.escidoc.core.resources.common.MetadataRecords;
import de.escidoc.core.resources.common.Result;
import de.escidoc.core.resources.common.properties.PublicStatus;
import de.escidoc.core.resources.common.reference.ContentModelRef;
import de.escidoc.core.resources.common.reference.ContextRef;
import de.escidoc.core.resources.om.container.Container;
import de.escidoc.core.resources.om.item.Item;

public class ByNameIngester extends AbstractIngester {

    private static final Logger LOG = LoggerFactory
	    .getLogger(ByNameIngester.class.getName());

    private List<String> names;

    private ResourceType resourceType;

    private List<ResourceEntry> result;

    public ByNameIngester(final URL eSciDocInfrastructureBaseUrl,
	    String userHandle, List<String> names, ResourceType resourceType) {
	super();
	init(eSciDocInfrastructureBaseUrl, userHandle, names, resourceType);
    }

    private void init(final URL eSciDocInfrastructureBaseUrl,
	    String userHandle, List<String> names, ResourceType resourceType) {

	try {
	    this.seteSciDocInfrastructureBaseUrl(eSciDocInfrastructureBaseUrl);
	    this.setUserHandle(userHandle);
	} catch (ConfigurationException e) {
	    LOG.error(
		    "Can not set infrastructure URL or user handle creating new Ingester.",
		    e);
	}
	this.names = names;
	this.resourceType = resourceType;
	this.result = new Vector<ResourceEntry>();

	loadConfiguration();
    }

    @Override
    protected void ingestHook() throws ConfigurationException, IngestException {

	try {
	    Iterator<String> nameIt = names.iterator();
	    while (nameIt.hasNext()) {
		String name = nameIt.next();

		if (this.resourceType == ResourceType.CONTENT_MODEL) {
		    ingestContentModel(name);
		} else if (this.resourceType == ResourceType.ITEM) {
		    ingestItem(name);
		} else if (this.resourceType == ResourceType.CONTAINER) {
		    ingestContainer(name);
		} else {
		    throw new IngestException(
			    "Can not ingest resource of type "
				    + this.resourceType);
		}
	    }
	} catch (InternalClientException e) {
	    String msg = "Error in eSciDoc Client.";
	    LOG.error(msg, e);
	    throw new IngestException(msg, e);
	} catch (EscidocException e) {
	    String msg = "Internal error in underlying eSciDoc Infrastructure.";
	    LOG.error(msg, e);
	    throw new IngestException(msg, e);
	} catch (TransportException e) {
	    // FIXME reason for Transport Exception?
	    String msg = "Communication error.";
	    LOG.error(msg, e);
	    throw new IngestException(msg, e);
	}
    }

    private void ingestContentModel(String name)
	    throws InternalClientException, EscidocException,
	    TransportException {
	// it's a create no ingest, there is no ingest method for CM

	ContentModel cm = new ContentModel();
	cm.setProperties(new ContentModelProperties());
	cm.getProperties().setName(name);
	cm.getProperties().setDescription("");

	ContentModel created = this.getContentModelHandlerClient().create(cm);
	this.storeResult(created);
    }

    private void ingestItem(String name) throws EscidocException,
	    InternalClientException, TransportException {

	Item item = new Item();

	// properties
	item.getProperties().setContentModel(
		new ContentModelRef(this.getItemContentModel()));
	item.getProperties().setContext(new ContextRef(this.getContext()));
	if (this.getInitialLifecycleStatus().equals("released")) {
	    item.getProperties().setPid("no:pid/test");
	}
	// this.getInitialLifecycleStatus()
	item.getProperties().setPublicStatus(PublicStatus.OPENED);
	item.getProperties().setPublicStatusComment(
		"Item ingested via Ingest Client API");

	item.setMetadataRecords(new MetadataRecords());
	item.getMetadataRecords().add(createOaiDcMetadata(name));

	// ingest
	MarshallerFactory mf = MarshallerFactory
		.getInstance(TransportProtocol.REST);
	Marshaller<Item> im = mf.getMarshaller(Item.class);
	String itemXml = im.marshalDocument(item);
	String result = this.getIngestHandlerClient().ingest(itemXml);

	// store result
	Marshaller<Result> rm = mf.getMarshaller(Result.class);
	Result r = rm.unmarshalDocument(result);
	String itemId = r.getFirst().getTextContent();
	Item created = this.getItemHandlerClient().retrieve(itemId);

	this.storeResult(created);
    }

    private void ingestContainer(String name) throws InternalClientException,
	    EscidocException, TransportException {

	Container container = new Container();

	// properties
	container.getProperties().setContentModel(
		new ContentModelRef(this.getContainerContentModel()));
	container.getProperties().setContext(new ContextRef(this.getContext()));
	if (this.getInitialLifecycleStatus().equals("released")) {
	    container.getProperties().setPid("no:pid/test");
	}
	container.getProperties().setPublicStatus(PublicStatus.OPENED);
	// this.getInitialLifecycleStatus());
	container.getProperties().setPublicStatusComment(
		"Container ingested via Ingest Client API");

	container.setMetadataRecords(new MetadataRecords());
	container.getMetadataRecords().add(createOaiDcMetadata(name));

	// ingest
	MarshallerFactory mf = MarshallerFactory
		.getInstance(TransportProtocol.REST);
	Marshaller<Container> cm = mf.getMarshaller(Container.class);
	String containerXml = cm.marshalDocument(container);
	String resultXml = this.getIngestHandlerClient().ingest(containerXml);

	// store result
	Marshaller<Result> rm = mf.getMarshaller(Result.class);
	Result r = rm.unmarshalDocument(resultXml);
	String containerId = r.getFirst().getTextContent();
	Container created = this.getContainerHandlerClient().retrieve(
		containerId);

	this.storeResult(created);
    }

    private MetadataRecord createOaiDcMetadata(String name) {
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
	dcTitle.setTextContent(name);
	oaiDc.appendChild(dcTitle);

	dcContentDocument.appendChild(oaiDc);

	MetadataRecord dc = new MetadataRecord("escidoc");
	dc.setContent(dcContentDocument.getDocumentElement());
	return dc;
    }

    private void storeResult(Resource r) {
	ResourceEntry re = new ResourceEntry();
	re.setIdentifier(r.getObjid());
	re.setObjectType(resourceType.name());
	re.setTitle(r.getXLinkTitle());
	re.setHref(r.getXLinkHref());

	result.add(re);
    }

    @Override
    public void checkConfiguration() throws ConfigurationException {

	if (this.geteSciDocInfrastructureBaseUrl() == null) {
	    throw new ConfigurationException(
		    "eSciDocInfrastructureBaseUrl must be set.");
	}

	if (this.getUserHandle() == null
		|| this.getUserHandle().trim().length() == 0) {
	    throw new ConfigurationException("userHandle must be set.");
	}

	if (this.resourceType.name().equals(ResourceType.CONTAINER.name())) {
	    if (this.getContext() == null
		    || this.getContext().trim().length() == 0) {
		throw new ConfigurationException("Context must be set.");
	    }

	    if (this.getContainerContentModel() == null
		    || this.getContainerContentModel().trim().length() == 0) {
		throw new ConfigurationException(
			"containerContentModel must be set.");
	    }
	}

	if (this.resourceType.name().equals(ResourceType.ITEM.name())) {
	    if (this.getContext() == null
		    || this.getContext().trim().length() == 0) {
		throw new ConfigurationException("Context must be set.");
	    }

	    if (this.getItemContentModel() == null
		    || this.getItemContentModel().trim().length() == 0) {
		throw new ConfigurationException(
			"itemContentModel must be set.");
	    }

	    if (this.getContentCategory() == null
		    || this.getContentCategory().trim().length() == 0) {
		throw new ConfigurationException("contentCategory must be set.");
	    }

	    if (this.getInitialLifecycleStatus() == null) {
		throw new ConfigurationException(
			"initialLifecycleStatus must be set.");
	    }

	    if (this.getMimeType() == null
		    || this.getMimeType().trim().length() == 0) {
		throw new ConfigurationException("Mime-Type must be set.");
	    }

	    if (this.getVisibility() == null
		    || this.getVisibility().trim().length() == 0) {
		throw new ConfigurationException(
			"Content visibility must be set.");
	    }

	    if (this.getValidStatus() == null
		    || this.getValidStatus().trim().length() == 0) {
		throw new ConfigurationException(
			"Content valid status must be set.");
	    }

	}
    }

    public List<ResourceEntry> getObjectIdentifier() {
	return result;
    }
}
