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

import de.escidoc.core.resources.common.properties.PublicStatus;

import org.escidoc.core.client.ingest.Ingester;
import org.escidoc.core.client.ingest.exceptions.AlreadyIngestedException;
import org.escidoc.core.client.ingest.exceptions.ConfigurationException;
import org.escidoc.core.client.ingest.exceptions.IngestException;
import org.escidoc.core.client.ingest.filesystem.DirectoryIngester;
import org.escidoc.core.client.ingest.model.IngestProgressListener;
import org.escidoc.core.client.ingest.model.IngesterBoundedRangeModel;

import de.escidoc.core.client.exceptions.EscidocException;
import de.escidoc.core.client.exceptions.InternalClientException;
import de.escidoc.core.client.exceptions.TransportException;

/**
 * Example of using {@link DirectoryIngester}.
 * 
 * @author Frank Schwichtenberg <Frank.Schwichtenberg@FIZ-Karlsruhe.de>
 * 
 */
public class ExampleDirectoryIngester {

    /**
     * @param args
     * @throws TransportException
     * @throws InternalClientException
     * @throws EscidocException
     * @throws AlreadyIngestedException
     * @throws ConfigurationException
     * @throws IngestException
     */
    public static void main(String[] args) throws EscidocException, InternalClientException, TransportException,
        ConfigurationException, AlreadyIngestedException, IngestException {

        Ingester ingester = new DirectoryIngester("http://localhost:8080", "Shibboleth-Handle-1", "/tmp/ingest");
        IngestProgressListener l = new IngesterBoundedRangeModel();
        ingester.setIngestProgressListener(l);

        ingester.setContainerContentModel(ingester.getContentModels().get(0).getIdentifier());
        ingester.setItemContentModel(ingester.getContentModels().get(0).getIdentifier());
        ingester.setContext(ingester.getContexts().get(0).getIdentifier());
        ingester.setContentCategory("ORIGINAL");
        ingester.setInitialLifecycleStatus(PublicStatus.RELEASED); // ingester.getLifecycleStatus().get(0));
        ingester.setMimeType("text/xml"); // ingester.getMimeTypes().get(0));
        // ingester.setVisibility("public");
        // ingester.setValidStatus("valid");

        System.out.println("ContainerContentModel[" + ingester.getContainerContentModel() + "]");
        System.out.println("ItemContentModel[" + ingester.getItemContentModel() + "]");
        System.out.println("Context[" + ingester.getContext() + "]");
        System.out.println("ContentCategory[" + ingester.getContentCategory() + "]");
        System.out.println("InitialLifecycleStatus[" + ingester.getInitialLifecycleStatus() + "]");
        System.out.println("MimeType[" + ingester.getMimeType() + "]");
        System.out.println("Visibility[" + ingester.getVisibility() + "]");
        System.out.println("ValidStatus[" + ingester.getValidStatus() + "]");

        ingester.ingest();
    }

}
