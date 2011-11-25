/**
 * CDDL HEADER START
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License, Version 1.0 only (the "License"). You may not use
 * this file except in compliance with the License.
 * 
 * You can obtain a copy of the license at license/ESCIDOC.LICENSE or
 * https://www.escidoc.org/license/ESCIDOC.LICENSE . See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at license/ESCIDOC.LICENSE. If applicable, add the
 * following below this CDDL HEADER, with the fields enclosed by brackets "[]"
 * replaced with your own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 * 
 * CDDL HEADER END
 * 
 * 
 * 
 * Copyright 2011 Fachinformationszentrum Karlsruhe Gesellschaft fuer
 * wissenschaftlich-technische Information mbH and Max-Planck- Gesellschaft zur
 * Foerderung der Wissenschaft e.V. All rights reserved. Use is subject to
 * license terms.
 */
package org.escidoc.core.tme;

import com.google.common.base.Preconditions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import javax.xml.parsers.ParserConfigurationException;

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

public class FileIngesterV2 {

    private final static Logger LOG = LoggerFactory.getLogger(FileIngesterV2.class);

    private ContentModelRef contentModelRef;

    private ContextRef contextRef;

    private IngestHandlerClientInterface ingestClient;

    private StagingHandlerClientInterface stagingClient;

    private TechnicalMetadataExtractor extractor;

    private ExecutorService executorService;

    public FileIngesterV2(ContextRef contextRef, ContentModelRef contentModelRef, URI serviceUri, String userHandle,
        File fitsHome) throws MalformedURLException, InternalClientException {
        Preconditions.checkNotNull(contextRef, "contextRef is null: %s", contextRef);
        Preconditions.checkNotNull(contentModelRef, "contentModelRef is null: %s", contentModelRef);
        Preconditions.checkNotNull(serviceUri, "serviceUri is null: %s", serviceUri);
        Preconditions.checkNotNull(userHandle, "userHandle is null: %s", userHandle);
        Preconditions.checkNotNull(fitsHome, "fitsHome is null: %s", fitsHome);

        this.contextRef = contextRef;
        this.contentModelRef = contentModelRef;

        stagingClient = new StagingHandlerClient(serviceUri.toURL());
        ingestClient = new IngestHandlerClient(serviceUri.toURL());
        extractor = new TechnicalMetadataExtractor(fitsHome);

        stagingClient.setHandle(userHandle);
        ingestClient.setHandle(userHandle);

        executorService = Executors.newFixedThreadPool(10);
    }

    public IngestResult ingest(File source) throws InternalClientException, FitsException, SAXException, IOException,
        ParserConfigurationException, EscidocException, TransportException {
        Preconditions.checkArgument(source.isFile(), source + " is not a file");

        LOG.debug("Extracting metadata..." + source.getName());
        Element extractedTme = extractor.extract(source);

        URL contentUrl = upload(source);

        LOG.debug("Ingesting..." + source.getName());
        return new IngestResult(ingestClient.ingest(Utils.itemToString(new ItemBuilder.Builder(contextRef,
            contentModelRef).withName(source.getName()).withContent(contentUrl, extractedTme).build())));
    }

    private URL upload(File source) throws EscidocException, InternalClientException, TransportException {
        LOG.debug("Uploading file to staging area..." + source.getName());
        URL contentUrl = stagingClient.upload(source);
        return contentUrl;
    }

    public IngestResult ingestAsync(final File source) throws InternalClientException, EscidocException,
        TransportException, InterruptedException, ExecutionException {

        LOG.debug("Extracting metadata..." + source.getName());
        FutureTask<Element> asyncTask = extractMetadataAsync(source);

        URL contentUrl = upload(source);

        LOG.debug("Ingesting..." + source.getName());
        return new IngestResult(ingestClient.ingest(Utils.itemToString(new ItemBuilder.Builder(contextRef,
            contentModelRef).withName(source.getName()).withContent(contentUrl, asyncTask.get()).build())));
    }

    private FutureTask<Element> extractMetadataAsync(final File source) {
        FutureTask<Element> extractMetadataTask = new FutureTask<Element>(new Callable<Element>() {
            @Override
            public Element call() {
                try {
                    return extractor.extract(source);
                }
                catch (FitsException e) {
                    LOG.error("Fail to extract technical metadata using FITS: " + e.getMessage(), e);
                    throw new RuntimeException("Fail to extract technical metadata using FITS: " + e.getMessage(), e);
                }
                catch (SAXException e) {
                    LOG.error("Fail to extract technical metadata using FITS: " + e.getMessage(), e);
                    throw new RuntimeException("Fail to extract technical metadata using FITS: " + e.getMessage(), e);
                }
                catch (IOException e) {
                    LOG.error("Fail to extract technical metadata using FITS: " + e.getMessage(), e);
                    throw new RuntimeException("Fail to extract technical metadata using FITS: " + e.getMessage(), e);
                }
                catch (ParserConfigurationException e) {
                    LOG.error("Fail to extract technical metadata using FITS: " + e.getMessage(), e);
                    throw new RuntimeException("Fail to extract technical metadata using FITS: " + e.getMessage(), e);
                }
            }
        });
        executorService.execute(extractMetadataTask);
        return extractMetadataTask;
    }
}