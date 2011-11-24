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

import java.io.File;
import java.net.URL;

import org.escidoc.core.client.ingest.filesystem.FileIngester;
import org.junit.Ignore;
import org.junit.Test;

import de.escidoc.core.client.Authentication;

public class FileIngestSpec {

    private static final String PARENT_CONTAINER_ID = null;

    private static final String INPUT_FULL_PATH = "/home/chh/ingest-me/1.3/Rest_api_doc_OM_Item.1.3.pdf";

    // private static final String INPUT_FULL_PATH =
    // "/Users/bender/ingest-me/SWORD-Intro.pdf";

    private static final String SERVICE_URL = "http://esfedrep1.fiz-karlsruhe.de:8080/";

    private static final String SYSADMIN = "sysadmin";

    private static final String SYSADMIN_PASSWORD = "eSciDoc";

    private static final String CONTEXT_ID = "escidoc:136";

    private static final String CONTAINER_CONTENT_MODEL = "escidoc:194";

    private static final String ITEM_CONTENT_MODEL = "escidoc:12";

    @Ignore
    @Test
    public void shouldExtractMetadataAndIngestFile() throws Exception {
        Authentication authentication = new Authentication(new URL(SERVICE_URL), SYSADMIN, SYSADMIN_PASSWORD);
        String userHandle = authentication.getHandle();
        // Given:
        FileIngester ingester = new FileIngester(SERVICE_URL, userHandle, PARENT_CONTAINER_ID);
        ingester.setContext(CONTEXT_ID);
        ingester.setContainerContentModel(CONTAINER_CONTENT_MODEL);
        ingester.setItemContentModel(ITEM_CONTENT_MODEL);
        ingester.setContentCategory("ORIGINAL");
        ingester.setMimeType("application/pdf");
        ingester.setVisibility("public");
        ingester.setValidStatus("valid");
        ingester.addFile(new File(INPUT_FULL_PATH));

        // When:
        ingester.ingest();

        // AssertThat:
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
