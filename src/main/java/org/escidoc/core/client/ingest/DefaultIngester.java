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

import org.escidoc.core.client.ingest.exceptions.ConfigurationException;
import org.escidoc.core.client.ingest.exceptions.IngestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;

import de.escidoc.core.client.TransportProtocol;
import de.escidoc.core.client.exceptions.EscidocException;
import de.escidoc.core.client.exceptions.InternalClientException;
import de.escidoc.core.client.exceptions.TransportException;
import de.escidoc.core.common.jibx.Marshaller;
import de.escidoc.core.common.jibx.MarshallerFactory;
import de.escidoc.core.resources.common.Result;

/**
 * Default implementation of an Ingester which is able to ingest (load) data into an eSciDoc Infrastructure. This
 * default ingester is just able to ingest from XML representations of an eSciDoc resource. Usually the ingester would
 * create the XML representation from given data in a form which is specific for the concrete ingester.
 * 
 * @see org.escidoc.core.client.ingest.Ingester
 * @see org.escidoc.core.client.ingest.AbstractIngester
 * 
 * @author Frank Schwichtenberg <Frank.Schwichtenberg@FIZ-Karlsruhe.de>
 * 
 */
public class DefaultIngester extends AbstractIngester {

    static final Logger LOG = LoggerFactory.getLogger(DefaultIngester.class.getName());

    private String resourceId;

    private String resourceXml;

    public DefaultIngester(final URL eSciDocInfrastructureBaseUrl, String userHandle) {
        super();
        init(eSciDocInfrastructureBaseUrl, userHandle);
    }

    public DefaultIngester(final URL eSciDocInfrastructureBaseUrl, String userHandle, File directory) {
        super();
        init(eSciDocInfrastructureBaseUrl, userHandle);
    }

    private void init(final URL eSciDocInfrastructureBaseUrl, final String userHandle) {

        try {
            this.seteSciDocInfrastructureBaseUrl(eSciDocInfrastructureBaseUrl);
            this.setUserHandle(userHandle);
        }
        catch (ConfigurationException e) {
            LOG.error("Can not set infrastructure URL or user handle creating new Ingester.", e);
        }

        loadConfiguration();
    }

    @Override
    protected void ingestHook() throws ConfigurationException, IngestException {
        try {
            String resultXml = this.getIngestHandlerClient().ingest(resourceXml);

            MarshallerFactory mf = MarshallerFactory.getInstance(TransportProtocol.REST);
            Marshaller<Result> rm = mf.getMarshaller(Result.class);
            Result result = rm.unmarshalDocument(resultXml);
            this.resourceId = result.getFirst().getTextContent();
        }
        catch (InternalClientException e) {
            String msg = "Error in eSciDoc Client.";
            LOG.error(msg, e);
            throw new IngestException(msg, e);
        }
        catch (EscidocException e) {
            String msg = "Internal error in underlying eSciDoc Infrastructure.";
            LOG.error(msg, e);
            throw new IngestException(msg, e);
        }
        catch (TransportException e) {
            // FIXME reason for Transport Exception?
            String msg = "Communication error.";
            LOG.error(msg, e);
            throw new IngestException(msg, e);
        }
        catch (DOMException e) {
            String msg = "Can not handle ingest result.";
            LOG.error(msg, e);
            throw new IngestException(msg, e);
        }
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceXml(String resourceXml) {
        this.resourceId = null;
        this.resourceXml = resourceXml;
    }
}
