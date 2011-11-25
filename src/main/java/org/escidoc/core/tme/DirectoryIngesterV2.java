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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import de.escidoc.core.client.exceptions.EscidocException;
import de.escidoc.core.client.exceptions.InternalClientException;
import de.escidoc.core.client.exceptions.TransportException;
import de.escidoc.core.resources.common.reference.ContentModelRef;
import de.escidoc.core.resources.common.reference.ContextRef;

public class DirectoryIngesterV2 {

    private final static Logger LOG = LoggerFactory.getLogger(DirectoryIngesterV2.class);

    private FileIngesterV2 fileIngesterV2;

    public DirectoryIngesterV2(ContextRef contextRef, ContentModelRef contentModelRef, URI serviceUri,
        String userHandle, File fitsHome) throws MalformedURLException, InternalClientException {

        fileIngesterV2 = new FileIngesterV2(contextRef, contentModelRef, serviceUri, userHandle, fitsHome);
    }

    public List<IngestResult> ingestAsync(File sourceDir) throws InterruptedException, ExecutionException {
        Preconditions.checkArgument(sourceDir.isDirectory(), sourceDir + " is not a Directory");
        List<FutureTask<IngestResult>> taskList = new ArrayList<FutureTask<IngestResult>>();

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (final File file : sourceDir.listFiles()) {
            FutureTask<IngestResult> fileIngestTask = createAsyncTask(file);
            executorService.execute(fileIngestTask);
            taskList.add(fileIngestTask);
        }

        List<IngestResult> list = new ArrayList<IngestResult>();
        for (FutureTask<IngestResult> futureTask : taskList) {
            IngestResult result = futureTask.get();
            LOG.debug("Finished..." + result);
            list.add(result);
        }
        return list;
    }

    private FutureTask<IngestResult> createAsyncTask(final File source) {
        FutureTask<IngestResult> future = new FutureTask<IngestResult>(new Callable<IngestResult>() {
            @Override
            public IngestResult call() {
                try {
                    return fileIngesterV2.ingestAsync(source);
                }
                catch (InternalClientException e) {
                    LOG.error("Fail to ingest file: " + source.getAbsolutePath(), e);
                    return new FailIngest(source, e);
                }
                catch (EscidocException e) {
                    LOG.error("Fail to ingest file: " + source.getAbsolutePath(), e);
                    return new FailIngest(source, e);
                }
                catch (TransportException e) {
                    LOG.error("Fail to ingest file: " + source.getAbsolutePath(), e);
                    return new FailIngest(source, e);
                }
                catch (InterruptedException e) {
                    LOG.error("Ingest file: " + source.getAbsolutePath() + " is interuppted", e);
                    return new FailIngest(source, e);
                }
                catch (ExecutionException e) {
                    LOG.error("Can not ingest file: " + source.getAbsolutePath(), e);
                    return new FailIngest(source, e);
                }
            }
        });
        return future;
    }
}