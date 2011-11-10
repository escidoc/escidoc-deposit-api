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
package org.escidoc.core.tme;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.base.Preconditions;

import de.escidoc.core.client.TransportProtocol;
import de.escidoc.core.client.exceptions.InternalClientException;
import de.escidoc.core.common.jibx.Marshaller;
import de.escidoc.core.common.jibx.MarshallerFactory;
import de.escidoc.core.resources.common.MetadataRecord;
import de.escidoc.core.resources.common.MetadataRecords;
import de.escidoc.core.resources.common.reference.ContentModelRef;
import de.escidoc.core.resources.common.reference.ContextRef;
import de.escidoc.core.resources.om.item.Item;

public class Utils {

    public static String createResourceXml(String contextId, String itemContentModelId, Element tmeContent)
        throws InternalClientException {
        Preconditions.checkNotNull(contextId, "contextId is null: %s", contextId);
        Preconditions.checkNotNull(itemContentModelId, "itemContentModelId is null: %s", itemContentModelId);

        Item item = new Item();

        // properties
        item.getProperties().setContentModel(new ContentModelRef(itemContentModelId));
        item.getProperties().setContext(new ContextRef(contextId));
        // item.getProperties().setPublicStatus(PublicStatus.OPENED);
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
        MetadataRecord tme = new MetadataRecord("TME");
        tme.setContent(tmeContent);
        item.getMetadataRecords().add(tme);

        return createMarshaller().marshalDocument(item);
    }

    public static String itemToString(Item item) throws InternalClientException {
        return createMarshaller().marshalDocument(item);
    }

    private static Marshaller<Item> createMarshaller() throws InternalClientException {
        MarshallerFactory mf = MarshallerFactory.getInstance(TransportProtocol.REST);
        Marshaller<Item> im = mf.getMarshaller(Item.class);
        return im;
    }

}
