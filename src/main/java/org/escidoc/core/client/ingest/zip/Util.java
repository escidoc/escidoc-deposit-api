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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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

import de.escidoc.core.client.Authentication;
import de.escidoc.core.client.IngestHandlerClient;
import de.escidoc.core.client.exceptions.EscidocException;
import de.escidoc.core.client.exceptions.InternalClientException;
import de.escidoc.core.client.exceptions.TransportException;

/**
 * CDDL HEADER START
 * 
 * The contents of this file are subject to the terms of the Common Development and Distribution License, Version 1.0
 * only (the "License"). You may not use this file except in compliance with the License.
 * 
 * You can obtain a copy of the license at license/ESCIDOC.LICENSE or https://www.escidoc.org/license/ESCIDOC.LICENSE .
 * See the License for the specific language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and include the License file at
 * license/ESCIDOC.LICENSE. If applicable, add the following below this CDDL HEADER, with the fields enclosed by
 * brackets "[]" replaced with your own identifying information: Portions Copyright [yyyy] [name of copyright owner]
 * 
 * CDDL HEADER END
 * 
 * 
 * 
 * Copyright 2011 Fachinformationszentrum Karlsruhe Gesellschaft fuer wissenschaftlich-technische Information mbH and
 * Max-Planck- Gesellschaft zur Foerderung der Wissenschaft e.V. All rights reserved. Use is subject to license terms.
 */

public final class Util {

    private static final Logger LOG = LoggerFactory.getLogger(Util.class);

    private static final int BUFFER_SIZE = 2048;

    private Util() {
        // Utility class
    }

    public static final void unzip(File zipFile, File targetDirectory) throws ZipException, IOException,
        EscidocException, InternalClientException, TransportException {
        Preconditions.checkNotNull(zipFile, "zipFile is null: %s", zipFile);
        Preconditions.checkNotNull(targetDirectory, "targetDirectory is null: %s", targetDirectory);
        Preconditions.checkArgument(targetDirectory.isDirectory(), "targetDirectory: " + targetDirectory
            + " is not a directory.");

        ZipFile zf = new ZipFile(zipFile);
        Enumeration<? extends ZipEntry> entries = zf.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            LOG.debug("Extracting: " + entry);

            File extractedFile = new File(targetDirectory, entry.getName());
            if (entry.isDirectory()) {
                extractedFile.mkdirs();
            }
            else {
                extractedFile.getParentFile().mkdirs();
                InputStream inputStream = zf.getInputStream(entry);
                ingest(inputStream);
                // copy(inputStream, extractedFile);
            }
        }
    }

    private static void ingest(InputStream inputStream) throws IOException, EscidocException, InternalClientException,
        TransportException {
        IngestHandlerClient ingestHandlerClient =
            new IngestHandlerClient(new URL("http://esfedrep1.fiz-karlsruhe.de:8080/"));

        ingestHandlerClient.setHandle(new Authentication(new URL("http://esfedrep1.fiz-karlsruhe.de:8080/"),
            "sysadmin", "eSciDoc").getHandle());
        StringWriter writer = new StringWriter();
        IOUtils.copy(inputStream, writer, "UTF-8");
        ingestHandlerClient.ingest(writer.toString());
    }

    private static void copy(InputStream inputStream, File extractedFile) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        byte[] buffer = new byte[BUFFER_SIZE];
        BufferedOutputStream target = new BufferedOutputStream(new FileOutputStream(extractedFile), BUFFER_SIZE);
        int readCount;
        while ((readCount = inputStream.read(buffer, 0, BUFFER_SIZE)) != -1) {
            target.write(buffer, 0, readCount);
        }
        target.flush();
        target.close();
        bis.close();

    }
}
