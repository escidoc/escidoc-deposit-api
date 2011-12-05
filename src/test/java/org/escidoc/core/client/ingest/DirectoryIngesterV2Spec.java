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
package org.escidoc.core.client.ingest;

import static org.junit.Assert.assertTrue;

import org.escidoc.core.tme.DirectoryIngesterV2;
import org.escidoc.core.tme.IngestResult;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.List;

import de.escidoc.core.client.Authentication;
import de.escidoc.core.resources.common.reference.ContentModelRef;
import de.escidoc.core.resources.common.reference.ContextRef;

public class DirectoryIngesterV2Spec {

    private final static Logger LOG = LoggerFactory.getLogger(DirectoryIngesterV2Spec.class);

    // private static final String INPUT_FULL_PATH = "/home/chh/ingest-me/1.3";

    private static final String INPUT_FULL_PATH = "/Users/bender/ingest-me/pictures/gnome-icon-theme-2.18.0/32x32/apps";

    // private static final String INPUT_FULL_PATH =
    // "/Users/bender/ingest-me/SWORD-Intro.pdf";

    private static final File fitsHome = new File("src/main/resources");

    private static final String SERVICE_URL = "http://esfedrep1.fiz-karlsruhe.de:8080/";

    private static final String SYSADMIN = "sysadmin";

    private static final String SYSADMIN_PASSWORD = "eSciDoc";

    private static final String CONTEXT_ID = "escidoc:136";

    private static final String ITEM_CONTENT_MODEL = "escidoc:12";

    @Test
    public void shouldGenerateHandle() throws Exception {
        long start = new Date().getTime();
        // Given:
        Authentication authentication = new Authentication(new URL(SERVICE_URL), SYSADMIN, SYSADMIN_PASSWORD);
        String handle = authentication.getHandle();
        LOG.debug("handle: " + handle);
    }

    @Ignore
    @Test
    public void shouldIngestFileAndItsTechnicalMetadataAsync() throws Exception {
        long start = new Date().getTime();
        // Given:
        Authentication authentication = new Authentication(new URL(SERVICE_URL), SYSADMIN, SYSADMIN_PASSWORD);
        ContextRef contextRef = new ContextRef(CONTEXT_ID);
        ContentModelRef contentModelRef = new ContentModelRef(ITEM_CONTENT_MODEL);
        String userHandle = authentication.getHandle();
        File source = new File(INPUT_FULL_PATH);
        URI serviceUri = new URI(SERVICE_URL);

        // When:
        DirectoryIngesterV2 ingester =
            new DirectoryIngesterV2(contextRef, contentModelRef, serviceUri, userHandle, fitsHome);
        List<IngestResult> result = ingester.ingestAsync(source);

        // AssertThat:
        assertTrue(!result.isEmpty());
        System.out.println("result" + result);

        long end = new Date().getTime();
        System.out.println("time: " + (end - start));
    }

    @Ignore
    @Test
    public void shouldIngestDirectory() throws Exception {
        long start = new Date().getTime();
        // Given:
        Authentication authentication = new Authentication(new URL(SERVICE_URL), SYSADMIN, SYSADMIN_PASSWORD);
        ContextRef contextRef = new ContextRef(CONTEXT_ID);
        ContentModelRef contentModelRef = new ContentModelRef(ITEM_CONTENT_MODEL);
        String userHandle = authentication.getHandle();
        File source = new File(INPUT_FULL_PATH);
        URI serviceUri = new URI(SERVICE_URL);

        // When:
        DirectoryIngesterV2 ingester =
            new DirectoryIngesterV2(contextRef, contentModelRef, serviceUri, userHandle, fitsHome);
        List<IngestResult> result = ingester.ingestAsync(source);

        // AssertThat:
        assertTrue(!result.isEmpty());
        System.out.println("result" + result);

        long end = new Date().getTime();
        System.out.println("time: " + (end - start));
    }
}
