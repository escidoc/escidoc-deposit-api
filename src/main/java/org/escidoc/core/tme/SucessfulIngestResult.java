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
package org.escidoc.core.tme;

import org.w3c.dom.DOMException;

import com.google.common.base.Preconditions;

import de.escidoc.core.client.TransportProtocol;
import de.escidoc.core.client.exceptions.InternalClientException;
import de.escidoc.core.common.jibx.MarshallerFactory;
import de.escidoc.core.resources.common.Result;

public class SucessfulIngestResult implements IngestResult {

    private String resultAsXml;

    public SucessfulIngestResult(String resultAsXml) {
        Preconditions.checkNotNull(resultAsXml, "resultAsXml is null: %s", resultAsXml);
        this.resultAsXml = resultAsXml;
    }

    public String getId() throws DOMException, InternalClientException {
        return MarshallerFactory
            .getInstance(TransportProtocol.REST).getMarshaller(Result.class).unmarshalDocument(resultAsXml).getFirst()
            .getTextContent();
    }

    @Override
    public boolean isSuccesful() {
        return true;
    }
}
