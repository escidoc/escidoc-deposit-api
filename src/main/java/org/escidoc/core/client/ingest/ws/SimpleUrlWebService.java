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

import org.escidoc.core.client.ingest.ws.exceptions.ExecutionException;

public class SimpleUrlWebService extends AbstractWebService {

    /**
     * Create a WebService instance for HTTP GET requests. Variables (words begining with '$') in the URL are replaced
     * by values with the variable name as key from given parameters.
     * 
     * @param url
     *            The URL to make a HTTP GET request to.
     */
    public SimpleUrlWebService(String url) {
        super();
    }

    @Override
    public Object call() throws ExecutionException {
        throw new UnsupportedOperationException("Not yet implemented.");
    }
}
