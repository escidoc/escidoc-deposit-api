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
package org.escidoc.core.client.ingest.examples;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import de.escidoc.core.resources.common.properties.PublicStatus;
import org.escidoc.core.client.ingest.DefaultIngester;
import org.escidoc.core.client.ingest.Ingester;
import org.escidoc.core.client.ingest.exceptions.ConfigurationException;
import org.escidoc.core.client.ingest.exceptions.IngestException;
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
import de.escidoc.core.resources.common.reference.ContentModelRef;
import de.escidoc.core.resources.common.reference.ContextRef;
import de.escidoc.core.resources.om.item.Item;

/**
 * Example of using {@link DefaultIngester}.
 * 
 * @author Frank Schwichtenberg <Frank.Schwichtenberg@FIZ-Karlsruhe.de>
 * 
 */
public class ExampleDefaultIngester {

    public static void main(String[] args) throws EscidocException, InternalClientException, TransportException,
        ConfigurationException, IngestException, MalformedURLException {

        // configure ingester (values are not used in default implementation but
        // creating resource xml below)
        Ingester ingester = new DefaultIngester(new URL("http://localhost:8080"), "Shibboleth-Handle-1");

        ingester.setContainerContentModel(ingester.getContentModels().get(0).getIdentifier());
        ingester.setItemContentModel(ingester.getContentModels().get(0).getIdentifier());
        ingester.setContext(ingester.getContexts().get(0).getIdentifier());
        ingester.setContentCategory("ORIGINAL");
        ingester.setInitialLifecycleStatus(PublicStatus.RELEASED);// ingester.getLifecycleStatus().get(0));
        ingester.setMimeType("text/xml");// ingester.getMimeTypes().get(0));
        ingester.setVisibility("public");
        ingester.setValidStatus("valid");

        // print out configuration
        System.out.println("ContainerContentModel[" + ingester.getContainerContentModel() + "]");
        System.out.println("ItemContentModel[" + ingester.getItemContentModel() + "]");
        System.out.println("Context[" + ingester.getContext() + "]");
        System.out.println("ContentCategory[" + ingester.getContentCategory() + "]");
        System.out.println("InitialLifecycleStatus[" + ingester.getInitialLifecycleStatus() + "]");
        System.out.println("MimeType[" + ingester.getMimeType() + "]");
        System.out.println("Visibility[" + ingester.getVisibility() + "]");
        System.out.println("ValidStatus[" + ingester.getValidStatus() + "]");

        // create resource XML for ingest
        String resourceXml = getResourceXml(ingester);

        // ingest
        ((DefaultIngester) ingester).setResourceXml(resourceXml);
        ingester.ingest();

        // get ID of ingested resource
        String id = ((DefaultIngester) ingester).getResourceId();
        System.out.println("Resource " + id + " created.");

    }

    public static String getResourceXml(Ingester ingester) throws InternalClientException {

        Item item = new Item();

        // properties
        item.getProperties().setContentModel(new ContentModelRef(ingester.getItemContentModel()));
        item.getProperties().setContext(new ContextRef(ingester.getContext()));
        if (ingester.getInitialLifecycleStatus().equals("released")) {
            item.getProperties().setPid("no:pid/test");
        }
        // TO abfrage vom ingester
        // ingester.getInitialLifecycleStatus());
        item.getProperties().setPublicStatus(PublicStatus.OPENED);
        item.getProperties().setPublicStatusComment("Item ingested via Ingest Client API");

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
        dcTitle.setTextContent("Item Title");
        oaiDc.appendChild(dcTitle);

        dcContentDocument.appendChild(oaiDc);

        MetadataRecord dc = new MetadataRecord("escidoc");
        dc.setContent(dcContentDocument.getDocumentElement());

        item.setMetadataRecords(new MetadataRecords());
        item.getMetadataRecords().add(dc);

        MarshallerFactory mf = MarshallerFactory.getInstance(TransportProtocol.REST);
        Marshaller<Item> im = mf.getMarshaller(Item.class);
        String itemXml = im.marshalDocument(item);

        return itemXml;
    }

}
