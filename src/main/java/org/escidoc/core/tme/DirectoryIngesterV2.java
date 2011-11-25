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
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import javax.xml.parsers.ParserConfigurationException;

import de.escidoc.core.client.IngestHandlerClient;
import de.escidoc.core.client.StagingHandlerClient;
import de.escidoc.core.client.exceptions.EscidocException;
import de.escidoc.core.client.exceptions.InternalClientException;
import de.escidoc.core.client.exceptions.TransportException;
import de.escidoc.core.resources.common.reference.ContentModelRef;
import de.escidoc.core.resources.common.reference.ContextRef;
import edu.harvard.hul.ois.fits.exceptions.FitsException;

public class DirectoryIngesterV2 {

    private final static Logger LOG = LoggerFactory.getLogger(DirectoryIngesterV2.class);

    private ContextRef contextRef;

    private ContentModelRef contentModelRef;

    private StagingHandlerClient stagingClient;

    private IngestHandlerClient ingestClient;

    private TechnicalMetadataExtractor extractor;

    private FileIngesterV2 fileIngesterV2;

    public DirectoryIngesterV2(ContextRef contextRef, ContentModelRef contentModelRef, URI serviceUri,
        String userHandle, File fitsHome) throws MalformedURLException, InternalClientException {
        this.contextRef = contextRef;
        this.contentModelRef = contentModelRef;

        stagingClient = new StagingHandlerClient(serviceUri.toURL());
        ingestClient = new IngestHandlerClient(serviceUri.toURL());
        extractor = new TechnicalMetadataExtractor(fitsHome);

        stagingClient.setHandle(userHandle);
        ingestClient.setHandle(userHandle);
        fileIngesterV2 = new FileIngesterV2(contextRef, contentModelRef, serviceUri, userHandle, fitsHome);
    }

    // 96449
    // time: 84031

    public List<String> ingest(File source) throws InternalClientException, FitsException, SAXException, IOException,
        ParserConfigurationException, EscidocException, TransportException, InterruptedException, ExecutionException {

        Preconditions.checkArgument(source.isDirectory(), source + " is not a Directory");
        File[] listFiles = source.listFiles();
        ArrayList<String> list = new ArrayList<String>();
        ArrayList<FutureTask> taskList = new ArrayList<FutureTask>();
        for (final File file : listFiles) {
            FutureTask<String> future = new FutureTask<String>(new Callable<String>() {
                @Override
                public String call() {
                    try {

                        return fileIngesterV2.ingestAsync(file);
                    }
                    catch (FitsException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    catch (SAXException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    catch (ParserConfigurationException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    catch (InternalClientException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    catch (EscidocException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    catch (TransportException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    catch (ExecutionException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    return null;
                }
            });
            Executors.newFixedThreadPool(10).execute(future);
            taskList.add(future);
            // String result = future.get();
            // list.add(result);
        }
        for (FutureTask<String> futureTask : taskList) {
            String result = futureTask.get();
            LOG.debug("Finished..." + result);
            list.add(result);
        }
        return list;
    }

}
