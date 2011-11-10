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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import de.escidoc.core.client.IngestHandlerClient;
import de.escidoc.core.client.exceptions.EscidocException;
import de.escidoc.core.client.exceptions.InternalClientException;
import de.escidoc.core.client.exceptions.TransportException;

public class ZipIngester {

    private static final Logger LOG = LoggerFactory.getLogger(ZipIngester.class);

    private static final String UTF_8_ENCODING = "UTF-8";

    private IngestHandlerClient client;

    public ZipIngester(URL escidocUrl, String handle) {
        Preconditions.checkNotNull(escidocUrl, "escidocUrl is null: %s", escidocUrl);
        Preconditions.checkNotNull(handle, "handle is null: %s", handle);

        client = new IngestHandlerClient(escidocUrl);
        client.setHandle(handle);
    }

    public void ingest(File zipFile) throws ZipException, IOException, EscidocException, InternalClientException,
        TransportException {
        Preconditions.checkNotNull(zipFile, "zipFile is null: %s", zipFile);

        ZipFile zf = new ZipFile(zipFile);
        Enumeration<? extends ZipEntry> entries = zf.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            LOG.debug("Extracting: " + entry);

            if (entry.isDirectory()) {
                throw new UnsupportedOperationException(entry + " is a directory. Not supported.");
            }

            ingest(zf.getInputStream(entry));
        }
    }

    private void ingest(InputStream inputStream) throws IOException, EscidocException, InternalClientException,
        TransportException {
        String result = client.ingest(stream2String(inputStream));
        LOG.debug("result: " + result);
    }

    private static String stream2String(InputStream inputStream) throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(inputStream, writer, UTF_8_ENCODING);
        return writer.toString();
    }
}