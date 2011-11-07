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

    private static final Logger LOG = LoggerFactory.getLogger(ByNameIngester.class.getName());

    private List<String> names;

    private ResourceType resourceType;

    private List<ResourceEntry> result;

    public ByNameIngester(final URL eSciDocInfrastructureBaseUrl, String userHandle, List<String> names,
        ResourceType resourceType) {
        super();
        init(eSciDocInfrastructureBaseUrl, userHandle, names, resourceType);
    }

    private void init(
        final URL eSciDocInfrastructureBaseUrl, String userHandle, List<String> names, ResourceType resourceType) {

        try {
            seteSciDocInfrastructureBaseUrl(eSciDocInfrastructureBaseUrl);
            setUserHandle(userHandle);
        }
        catch (ConfigurationException e) {
            LOG.error("Can not set infrastructure URL or user handle creating new Ingester.", e);
        }
        this.names = names;
        this.resourceType = resourceType;
        result = new Vector<ResourceEntry>();

        loadConfiguration();
    }

    @Override
    protected void ingestHook() throws ConfigurationException, IngestException {

        try {
            Iterator<String> nameIt = names.iterator();
            while (nameIt.hasNext()) {
                String name = nameIt.next();

                if (resourceType == ResourceType.CONTENT_MODEL) {
                    ingestContentModel(name);
                }
                else if (resourceType == ResourceType.ITEM) {
                    ingestItem(name);
                }
                else if (resourceType == ResourceType.CONTAINER) {
                    ingestContainer(name);
                }
                else {
                    throw new IngestException("Can not ingest resource of type " + resourceType);
                }
            }
        }
        catch (InternalClientException e) {
            String msg = "Error in eSciDoc Client.";
            LOG.error(msg, e);
            throw new IngestException(msg, e);
        }
        catch (EscidocException e) {
            String msg = "Internal error in underlying eSciDoc Infrastructure.";
            LOG.error(msg, e);
            throw new IngestException(msg, e);
        }
        catch (TransportException e) {
            // FIXME reason for Transport Exception?
            String msg = "Communication error.";
            LOG.error(msg, e);
            throw new IngestException(msg, e);
        }
    }

    private void ingestContentModel(String name) throws InternalClientException, EscidocException, TransportException {
        // it's a create no ingest, there is no ingest method for CM

        ContentModel cm = new ContentModel();
        cm.setProperties(new ContentModelProperties());
        cm.getProperties().setName(name);
        cm.getProperties().setDescription("");

        ContentModel created = getContentModelHandlerClient().create(cm);
        storeResult(created);
    }

    private void ingestItem(String name) throws EscidocException, InternalClientException, TransportException {

        Item item = new Item();

        // properties
        item.getProperties().setContentModel(new ContentModelRef(getItemContentModel()));
        item.getProperties().setContext(new ContextRef(getContext()));
        if (getInitialLifecycleStatus().equals("released")) {
            item.getProperties().setPid("no:pid/test");
        }
        // this.getInitialLifecycleStatus()
        item.getProperties().setPublicStatus(PublicStatus.OPENED);
        item.getProperties().setPublicStatusComment("Item ingested via Ingest Client API");

        item.setMetadataRecords(new MetadataRecords());
        item.getMetadataRecords().add(createOaiDcMetadata(name));

        // ingest
        MarshallerFactory mf = MarshallerFactory.getInstance(TransportProtocol.REST);
        Marshaller<Item> im = mf.getMarshaller(Item.class);
        String itemXml = im.marshalDocument(item);
        String result = getIngestHandlerClient().ingest(itemXml);

        // store result
        Marshaller<Result> rm = mf.getMarshaller(Result.class);
        Result r = rm.unmarshalDocument(result);
        String itemId = r.getFirst().getTextContent();
        Item created = getItemHandlerClient().retrieve(itemId);

        storeResult(created);
    }

    private void ingestContainer(String name) throws InternalClientException, EscidocException, TransportException {

        Container container = new Container();

        // properties
        container.getProperties().setContentModel(new ContentModelRef(getContainerContentModel()));
        container.getProperties().setContext(new ContextRef(getContext()));
        if (getInitialLifecycleStatus().equals("released")) {
            container.getProperties().setPid("no:pid/test");
        }
        container.getProperties().setPublicStatus(PublicStatus.OPENED);
        // this.getInitialLifecycleStatus());
        container.getProperties().setPublicStatusComment("Container ingested via Ingest Client API");

        container.setMetadataRecords(new MetadataRecords());
        container.getMetadataRecords().add(createOaiDcMetadata(name));

        // ingest
        MarshallerFactory mf = MarshallerFactory.getInstance(TransportProtocol.REST);
        Marshaller<Container> cm = mf.getMarshaller(Container.class);
        String containerXml = cm.marshalDocument(container);
        String resultXml = getIngestHandlerClient().ingest(containerXml);

        // store result
        Marshaller<Result> rm = mf.getMarshaller(Result.class);
        Result r = rm.unmarshalDocument(resultXml);
        String containerId = r.getFirst().getTextContent();
        Container created = getContainerHandlerClient().retrieve(containerId);

        storeResult(created);
    }

    private MetadataRecord createOaiDcMetadata(String name) {
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

        if (geteSciDocInfrastructureBaseUrl() == null) {
            throw new ConfigurationException("eSciDocInfrastructureBaseUrl must be set.");
        }

        if (getUserHandle() == null || getUserHandle().trim().length() == 0) {
            throw new ConfigurationException("userHandle must be set.");
        }

        if (resourceType.name().equals(ResourceType.CONTAINER.name())) {
            if (getContext() == null || getContext().trim().length() == 0) {
                throw new ConfigurationException("Context must be set.");
            }

            if (getContainerContentModel() == null || getContainerContentModel().trim().length() == 0) {
                throw new ConfigurationException("containerContentModel must be set.");
            }
        }

        if (resourceType.name().equals(ResourceType.ITEM.name())) {
            if (getContext() == null || getContext().trim().length() == 0) {
                throw new ConfigurationException("Context must be set.");
            }

            if (getItemContentModel() == null || getItemContentModel().trim().length() == 0) {
                throw new ConfigurationException("itemContentModel must be set.");
            }

            if (getContentCategory() == null || getContentCategory().trim().length() == 0) {
                throw new ConfigurationException("contentCategory must be set.");
            }

            if (getInitialLifecycleStatus() == null) {
                throw new ConfigurationException("initialLifecycleStatus must be set.");
            }

            if (getMimeType() == null || getMimeType().trim().length() == 0) {
                throw new ConfigurationException("Mime-Type must be set.");
            }

            if (getVisibility() == null || getVisibility().trim().length() == 0) {
                throw new ConfigurationException("Content visibility must be set.");
            }

            if (getValidStatus() == null || getValidStatus().trim().length() == 0) {
                throw new ConfigurationException("Content valid status must be set.");
            }

        }
    }

    public List<ResourceEntry> getObjectIdentifier() {
        return result;
    }
}
