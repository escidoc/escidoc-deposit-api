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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.common.base.Preconditions;

import de.escidoc.core.client.IngestHandlerClient;
import de.escidoc.core.client.StagingHandlerClient;
import de.escidoc.core.client.exceptions.EscidocException;
import de.escidoc.core.client.exceptions.InternalClientException;
import de.escidoc.core.client.exceptions.TransportException;
import de.escidoc.core.client.interfaces.IngestHandlerClientInterface;
import de.escidoc.core.client.interfaces.StagingHandlerClientInterface;
import de.escidoc.core.resources.common.reference.ContentModelRef;
import de.escidoc.core.resources.common.reference.ContextRef;
import edu.harvard.hul.ois.fits.exceptions.FitsException;

public class FileIngester {

    private ContentModelRef contentModelRef;

    private ContextRef contextRef;

    private File source;

    private IngestHandlerClientInterface ingestClient;

    private StagingHandlerClientInterface stagingClient;

    private TechnicalMetadataExtractor extractor;

    public FileIngester(File source, ContextRef contextRef, ContentModelRef contentModelRef, URI serviceUri,
        String userHandle, File fitsHome) throws MalformedURLException, InternalClientException {
        this.source = source;
        this.contextRef = contextRef;
        this.contentModelRef = contentModelRef;

        Preconditions.checkArgument(source.isFile(), source + " is not a file");

        stagingClient = new StagingHandlerClient(serviceUri.toURL());
        ingestClient = new IngestHandlerClient(serviceUri.toURL());
        extractor = new TechnicalMetadataExtractor(fitsHome);

        stagingClient.setHandle(userHandle);
        ingestClient.setHandle(userHandle);

    }

    public String ingest() throws InternalClientException, FitsException, SAXException, IOException,
        ParserConfigurationException, EscidocException, TransportException {

        Element extractedTme = extractor.extract(source);
        URL contentUrl = stagingClient.upload(source);

        return ingestClient.ingest(Utils.itemToString(new ItemBuilder.Builder(contextRef, contentModelRef)
            .withName(source.getName()).withContent(contentUrl, extractedTme).build()));
    }
}
