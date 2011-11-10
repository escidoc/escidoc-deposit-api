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
package org.escidoc.core.client.ingest.zip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.escidoc.core.client.ingest.AbstractIngester;
import org.escidoc.core.client.ingest.exceptions.ConfigurationException;
import org.escidoc.core.client.ingest.exceptions.IngestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import de.escidoc.core.client.IngestHandlerClient;
import de.escidoc.core.client.exceptions.EscidocException;
import de.escidoc.core.client.exceptions.InternalClientException;
import de.escidoc.core.client.exceptions.TransportException;

public class ZipIngester extends AbstractIngester {

    private static final Logger LOG = LoggerFactory.getLogger(ZipIngester.class);

    private static final String UTF_8_ENCODING = "UTF-8";

    private IngestHandlerClient client;

    public ZipIngester(URL escidocUrl, String handle) {
        Preconditions.checkNotNull(escidocUrl, "escidocUrl is null: %s", escidocUrl);
        Preconditions.checkNotNull(handle, "handle is null: %s", handle);

        client = new IngestHandlerClient(escidocUrl);
        client.setHandle(handle);
    }

    public void ingest(InputStream inputStream) throws EscidocException, InternalClientException, TransportException,
        UnsupportedEncodingException, IOException {
        Preconditions.checkNotNull(inputStream, "inputStream is null: %s", inputStream);
        ZipInputStream zis = new ZipInputStream(inputStream);
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            if (entry.isDirectory()) {
                throw new IllegalArgumentException("Extracting ZIP File that contains directory is not supported");
            }

            LOG.debug("Extracting: " + entry);
            byte[] buffer = new byte[1024];
            int bytesRead;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            while ((bytesRead = zis.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            ingest(new String(outputStream.toByteArray(), UTF_8_ENCODING));
        }
    }

    private void ingest(String resourceXml) throws EscidocException, InternalClientException, TransportException {
        client.ingest(resourceXml);
    }

    @Override
    protected void ingestHook() throws ConfigurationException, IngestException {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}