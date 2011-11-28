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

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Date;

import org.escidoc.core.tme.FileIngesterV2;
import org.escidoc.core.tme.SucessfulIngestResult;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.escidoc.core.client.Authentication;
import de.escidoc.core.resources.common.reference.ContentModelRef;
import de.escidoc.core.resources.common.reference.ContextRef;

public class FileIngestV2Spec {

    private final static Logger LOG = LoggerFactory.getLogger(FileIngestV2Spec.class);

    // private static final String INPUT_FULL_PATH =
    // "/home/chh/ingest-me/1.3/Rest_api_doc_OM_Item.1.3.pdf";

    private static final String INPUT_FULL_PATH =
        "/Users/bender/ingest-me/pictures/gnome-icon-theme-2.18.0/32x32/apps/accessories-calculator.png";

    private static final File fitsHome = new File("src/main/resources");

    private static final String SERVICE_URL = "http://esfedrep1.fiz-karlsruhe.de:8080/";

    private static final String SYSADMIN = "sysadmin";

    private static final String SYSADMIN_PASSWORD = "eSciDoc";

    private static final String CONTEXT_ID = "escidoc:136";

    private static final String ITEM_CONTENT_MODEL = "escidoc:12";

    private Authentication authentication;

    private ContextRef contextRef;

    private ContentModelRef contentModelRef;

    private String userHandle;

    private File source;

    private URI serviceUri;

    private FileIngesterV2 ingester;

    @Before
    public void setup() throws Exception {
        authentication = new Authentication(new URL(SERVICE_URL), SYSADMIN, SYSADMIN_PASSWORD);
        contextRef = new ContextRef(CONTEXT_ID);
        contentModelRef = new ContentModelRef(ITEM_CONTENT_MODEL);
        userHandle = authentication.getHandle();
        source = new File(INPUT_FULL_PATH);
        serviceUri = new URI(SERVICE_URL);
        ingester = new FileIngesterV2(contextRef, contentModelRef, serviceUri, userHandle, fitsHome);
    }

    @Ignore
    @Test
    public void shouldIngestFileAndItsTechnicalMetadata() throws Exception {
        for (int i = 0; i < 4; i++) {
            long start = new Date().getTime();

            // When:
            SucessfulIngestResult result = ingester.ingest(source);

            // AssertThat:
            assertTrue(!result.getId().isEmpty());
            LOG.debug("result" + result);

            long end = new Date().getTime();
            LOG.debug("total time: " + (end - start) + " ms");
        }
    }

    @Ignore
    @Test
    public void shouldIngestFileAndItsTechnicalMetadataAsync() throws Exception {
        for (int i = 0; i < 4; i++) {
            long start = new Date().getTime();

            // When:
            SucessfulIngestResult result = ingester.ingestAsync(source);

            // AssertThat:
            assertTrue(!result.getId().isEmpty());
            LOG.debug("result" + result);

            long end = new Date().getTime();
            LOG.debug("total time: " + (end - start) + " ms");
        }
    }
}