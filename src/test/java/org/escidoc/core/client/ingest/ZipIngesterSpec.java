package org.escidoc.core.client.ingest;
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

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

import org.escidoc.core.client.ingest.zip.Util;
import org.escidoc.core.client.ingest.zip.ZipIngester;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.escidoc.core.client.Authentication;

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

public class ZipIngesterSpec {

    private static final Logger LOG = LoggerFactory.getLogger(ZipIngesterSpec.class);

    private static final String TARGET_URL = "http://esfedrep1.fiz-karlsruhe.de:8080/";

    private static final String SOURCE_DIR = "dropbox/Public/";

    private static final String FILE_NAME = "elab-content-models.zip";

    // @Test
    public void shouldOnlyTakeZipFileAsAnInput() throws Exception {
        Util.unzip(getZipFile(), getSourceDir());
    }

    @Test
    public void shouldIngestDirectlyFromZipFile() throws Exception {
        new ZipIngester(new URL(TARGET_URL), new Authentication(new URL(TARGET_URL), "sysadmin", "eSciDoc").getHandle())
            .ingest(new FileInputStream(getZipFile()));
    }

    private File getZipFile() {
        return new File(getSourceDir(), FILE_NAME);
    }

    private File getSourceDir() {
        return new File(new File(System.getProperty("user.home")), SOURCE_DIR);
    }
}