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
package org.escidoc.core.client.ingest.ws;

import java.io.File;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.escidoc.core.client.ingest.ws.exceptions.ExecutionException;

public abstract class AbstractWebService implements WebService {

    protected Map<String, String> params;

    protected File file;

    protected String mimeType = "application/octet-stream";

    protected AbstractWebService() {
        params = new HashMap<String, String>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.escidoc.core.client.ws.WebService#call()
     */
    @Override
    public abstract Object call() throws ExecutionException;

    /*
     * (non-Javadoc)
     * 
     * @see org.escidoc.core.client.ws.WebService#addParam(java.lang.String, java.lang.String)
     */
    @Override
    public void addParam(String key, String value) {
        params.put(key, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.escidoc.core.client.ws.WebService#addParams(java.util.Map)
     */
    @Override
    public void addParams(Map<Object, Object> params) {
        for (Entry<Object, Object> entry : params.entrySet()) {
            addParam(entry.toString(), entry.getValue().toString());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.escidoc.core.client.ws.WebService#addParams(java.io.File)
     */
    @Override
    public void addParams(File file) throws MalformedURLException {
        this.file = file;
        params.put("localpath", file.getPath());
        params.put("name", file.getName());
        params.put("url", file.toURI().toURL().toString());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.escidoc.core.client.ws.WebService#setMimeType(java.lang.String)
     */
    @Override
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
