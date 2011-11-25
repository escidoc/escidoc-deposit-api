package org.escidoc.core.client.ingest;

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
import org.escidoc.core.client.ingest.filesystem.DirectoryIngester;
import org.escidoc.core.client.ingest.model.IngesterBoundedRangeModel;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;

import de.escidoc.core.client.Authentication;
import de.escidoc.core.resources.common.properties.PublicStatus;

public class DirectoryIngesterSpec {

    private static final Logger LOG = LoggerFactory.getLogger(DirectoryIngesterSpec.class);

    private static final String coreUrl = "http://esfedrep1.fiz-karlsruhe.de:8080";

    private static final String SYSADMIN = "sysadmin";

    private static final String PASSWORD = "eSciDoc";

    private static final String DIRECTORY_PATH = "ingest-me";

    private static final String CONTEXT_ID = "escidoc:136";

    private static final String CONTAINER_CONTENT_MODEL_ID = "escidoc:13";

    private static final String ITEM_CONTENT_MODEL_ID = "escidoc:12";

    private static final String VALID_STATUS = "valid";

    private static final String PUBLIC_CONTENT_VISIBILITY = "public";

    private static final String PDF_MIME_TYPE = "image/png";

    private static final String CONTENT_CATEGORY = "ORIGINAL";

    // @Ignore
    @Test
    public void shouldIngestDirectoryWithOneFile() throws Exception {
        // String fullpath = System.getProperty("user.home") +
        // System.getProperty("file.separator") + DIRECTORY_PATH;
        String fullpath = "/Users/bender/ingest-me/pictures/gnome-icon-theme-2.18.0/32x32/apps";
        LOG.debug("fullpath: " + fullpath);
        // Given:
        final Ingester ingester =
            new DirectoryIngester(coreUrl, new Authentication(new URL(coreUrl), SYSADMIN, PASSWORD).getHandle(),
                new File(fullpath));
        ingester.setContext(CONTEXT_ID);
        ingester.setContainerContentModel(CONTAINER_CONTENT_MODEL_ID);
        ingester.setItemContentModel(ITEM_CONTENT_MODEL_ID);
        ingester.setContentCategory(CONTENT_CATEGORY);
        ingester.setInitialLifecycleStatus(PublicStatus.RELEASED);
        ingester.setMimeType(PDF_MIME_TYPE);
        ingester.setVisibility(PUBLIC_CONTENT_VISIBILITY);
        ingester.setValidStatus(VALID_STATUS);
        ingester.setIngestProgressListener(new IngesterBoundedRangeModel());
        // When:
        ingester.ingest();
        // AssertThat:
        throw new UnsupportedOperationException("Not yet implemented");
    }
}