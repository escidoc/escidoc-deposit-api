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

import java.net.URL;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.base.Preconditions;

import de.escidoc.core.resources.common.MetadataRecord;
import de.escidoc.core.resources.common.MetadataRecords;
import de.escidoc.core.resources.common.reference.ContentModelRef;
import de.escidoc.core.resources.common.reference.ContextRef;
import de.escidoc.core.resources.om.item.Item;
import de.escidoc.core.resources.om.item.ItemProperties;
import de.escidoc.core.resources.om.item.StorageType;
import de.escidoc.core.resources.om.item.component.Component;
import de.escidoc.core.resources.om.item.component.ComponentContent;
import de.escidoc.core.resources.om.item.component.ComponentProperties;
import de.escidoc.core.resources.om.item.component.Components;

public class ItemBuilder {

    public static class Builder {

        private static final String ESCIDOC = "escidoc";

        private static final String DC_NAMESPACE = "http://purl.org/dc/elements/1.1/";

        private static final Logger LOG = LoggerFactory.getLogger(ItemBuilder.Builder.class);

        private final Item item = new Item();

        private final MetadataRecords componentMetadataList = new MetadataRecords();

        private final ItemProperties itemProps = new ItemProperties();

        private final Components componentList = new Components();

        private final Component component = new Component();

        private final ComponentContent content = new ComponentContent();

        private final ComponentProperties componentProps = new ComponentProperties();

        private final ContextRef contextRef;

        private final ContentModelRef contentModelRef;

        private URL contentUrl;

        private String itemName;

        private MetadataRecord itemMetadata;

        private Element tme;

        private MetadataRecord componentMetadata;

        public Builder(final ContextRef contextRef, final ContentModelRef contentModelRef) {
            Preconditions.checkNotNull(contextRef, "contextRef is null: %s", contextRef);
            Preconditions.checkNotNull(contentModelRef, "contentModelRef is null: %s", contentModelRef);
            this.contextRef = contextRef;
            this.contentModelRef = contentModelRef;
        }

        public Builder withContent(URL contentUrl, Element tme) {
            Preconditions.checkNotNull(contentUrl, "content is null: %s", contentUrl);
            Preconditions.checkNotNull(tme, "tme is null: %s", tme);
            this.contentUrl = contentUrl;
            this.tme = tme;
            return this;
        }

        public Builder withName(String name) {
            itemName = name;
            return this;
        }

        public Builder withMimeType(String mimeType) {
            if (mimeType == null) {
                return this;
            }
            Preconditions.checkArgument(!mimeType.isEmpty(), "mimeType can not be empty String");
            componentProps.setMimeType(mimeType);
            return this;
        }

        public Item build() {
            return tryBuildNewItem();
        }

        private Item tryBuildNewItem() {
            try {
                setItemName();
                setItemProperties();
                setComponent();
                return item;
            }
            catch (final ParserConfigurationException e) {
                LOG.error("Error creating a XML document. " + e.getMessage(), e);
                throw new RuntimeException(e);
            }

        }

        private void setComponent() throws ParserConfigurationException {
            addComponents(item, contentUrl);
        }

        private void setItemName() throws ParserConfigurationException {
            addDefaultMetadata(createNewDocument());
        }

        private Document createNewDocument() throws ParserConfigurationException {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        }

        private void setItemProperties() {
            itemProps.setContext(contextRef);
            itemProps.setContentModel(contentModelRef);
            item.setProperties(itemProps);
        }

        private void addDefaultMetadata(final Document doc) {
            buildDefaultMetadata(doc);
            final MetadataRecords itemMetadataList = new MetadataRecords();
            itemMetadataList.add(itemMetadata);
            item.setMetadataRecords(itemMetadataList);
        }

        private void buildDefaultMetadata(final Document doc) {
            itemMetadata = new MetadataRecord(ESCIDOC);
            itemMetadata.setContent(buildContentForItemMetadata(doc));
        }

        private Element buildContentForItemMetadata(final Document doc) {
            final Element element = doc.createElementNS(DC_NAMESPACE, "dc");
            final Element titleElmt = doc.createElementNS(DC_NAMESPACE, "title");
            titleElmt.setPrefix("dc");
            titleElmt.setTextContent(itemName);
            element.appendChild(titleElmt);
            return element;
        }

        private void addComponents(final Item item, final URL contentRef) throws ParserConfigurationException {
            setComponentProperties(component);
            setComponentContent(component, contentRef);
            setComponentTitle(component);
            setComponentTme(tme);
            componentList.add(component);
            item.setComponents(componentList);
        }

        private void setComponentTme(Element tme) {
            MetadataRecord mr = new MetadataRecord("TME");
            mr.setContent(tme);
            componentMetadataList.add(mr);
        }

        private void setComponentTitle(Component component) throws ParserConfigurationException {
            final Document doc = createNewDocument();
            final Element element = buildContentForItemMetadata(doc);
            componentMetadata = new MetadataRecord(ESCIDOC);
            componentMetadata.setContent(element);
            componentMetadataList.add(componentMetadata);
            component.setMetadataRecords(componentMetadataList);
        }

        private void setComponentContent(final Component component, final URL contentRef) {
            content.setXLinkHref(contentRef.toString());
            content.setStorage(StorageType.INTERNAL_MANAGED);
            component.setContent(content);
        }

        private void setComponentProperties(final Component component) {
            componentProps.setDescription("Description?");
            componentProps.setFileName(itemName);
            componentProps.setVisibility("isVisible?");
            componentProps.setContentCategory("which one?");
            component.setProperties(componentProps);
        }

    }
}