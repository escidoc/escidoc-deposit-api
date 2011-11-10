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

import java.util.List;

import org.escidoc.core.client.ingest.entities.ResourceEntry;
import org.escidoc.core.client.ingest.exceptions.ConfigurationException;
import org.escidoc.core.client.ingest.exceptions.IngestException;
import org.escidoc.core.client.ingest.model.IngestProgressListener;
import org.escidoc.core.client.ingest.model.IngesterBoundedRangeModel;

import de.escidoc.core.client.exceptions.EscidocException;
import de.escidoc.core.client.exceptions.InternalClientException;
import de.escidoc.core.client.exceptions.TransportException;
import de.escidoc.core.resources.common.properties.PublicStatus;

/**
 * An Ingester which is able to ingest (load) data into an eSciDoc Infrastructure.
 * 
 * @see org.escidoc.core.client.ingest.AbstractIngester
 * @see org.escidoc.core.client.ingest.DefaultIngester
 * 
 * @author Frank Schwichtenberg &lt;Frank.Schwichtenberg@FIZ-Karlsruhe.de&gt;
 * 
 */
public interface Ingester {

    /**
     * The list of available Content Models. In a first implementation those are configured locally.
     * 
     * @return A list of available Content Models in the underlying eSciDoc Infrastructure. Not filtered by access
     *         rights.
     * 
     * @throws InternalClientException
     *             If an internal error in the eSciDoc Client Library occurs.
     * @throws EscidocException
     *             If an error occurs in the underlying eSciDoc Infrastructure.
     * @throws TransportException
     *             If an transport error in the eSciDoc Client Library occurs.
     */
    List<ResourceEntry> getContentModels() throws EscidocException, InternalClientException, TransportException;

    /**
     * The list of available Contexts. In a first implementation those are configured locally.
     * 
     * @return A list of available Contexts in the underlying eSciDoc Infrastructure. Not filtered by access rights.
     * 
     * @throws InternalClientException
     *             If an internal error in the eSciDoc Client Library occurs.
     * @throws EscidocException
     *             If an error occurs in the underlying eSciDoc Infrastructure.
     * @throws TransportException
     *             If an transport error in the eSciDoc Client Library occurs.
     */
    List<ResourceEntry> getContexts() throws InternalClientException, EscidocException, TransportException;

    /**
     * The list of IANA mime-types. May be deprecated before first release because CoNE Service should be used.
     * 
     * @return List of strings representing mime-types. E.g. "text/plain".
     */
    List<String> getMimeTypes();

    /**
     * The list of available lifecycle status.
     * 
     * @return List of strings, the lifecycle names. E.g. "pending".
     */
    List<PublicStatus> getLifecycleStatus();

    /**
     * Do the ingest. Checks if all necessary settings are done and if ingest is not already started. In the first
     * implementation only one successful ingest per Ingester is possible.
     * 
     * @throws ConfigurationException
     *             If this Ingester is not sufficient configured.
     * @throws IngestException
     *             If an error occurs while ingesting.
     */
    void ingest() throws ConfigurationException, IngestException;

    /*
     * getters/setters
     */

    /**
     * @return The ID of the Context that will be set for an ingested resource.
     */
    String getContext();

    /**
     * @param context
     *            The ID of the Context that will be set for an ingested resource.
     */
    void setContext(String context);

    /**
     * @return The ID of the Content Model that will be set for an ingested Container.
     */
    String getContainerContentModel();

    /**
     * @param containerContentModel
     *            The ID of the Content Model that will be set for an ingested Container.
     */
    void setContainerContentModel(String containerContentModel);

    /**
     * @return The ID of the Content Model that will be set for an ingested Item.
     */
    String getItemContentModel();

    /**
     * @param itemContentModel
     *            The ID of the Content Model that will be set for an ingested Item.
     */
    void setItemContentModel(String itemContentModel);

    /**
     * @return The content category that will be set for the content of an ingested Item.
     */
    String getContentCategory();

    /**
     * @param contentCategory
     *            The content category that will be set for the content of an ingested Item.
     */
    void setContentCategory(String contentCategory);

    /**
     * @return The lifecycle status that will be set for an ingested resource.
     */
    PublicStatus getInitialLifecycleStatus();

    /**
     * @param initialLifecycleStatus
     *            The lifecycle status that will be set for an ingested resource.
     */
    void setInitialLifecycleStatus(PublicStatus initialLifecycleStatus);

    /**
     * @return The mime-type that will be set for the content of an ingested Item.
     */
    String getMimeType();

    /**
     * @param mimeType
     *            The mime-type that will be set for the content of an ingested Item.
     */
    void setMimeType(String mimeType);

    /**
     * @return The visibility that will be set for the content of an ingested Item.
     */
    String getVisibility();

    /**
     * @param visibility
     *            The visibility that will be set for the content of an ingested Item.
     */
    void setVisibility(String visibility);

    /**
     * @return The valid status that will be set for the content of an ingested Item.
     */
    String getValidStatus();

    /**
     * @param validStatus
     *            The valid status that will be set for the content of an ingested Item.
     */
    void setValidStatus(String validStatus);

    /**
     * @param ingestProgressListener
     *            A listener that should be updated by a concrete ingester if set. // TODO don't call it listener!?
     * 
     * @see IngesterBoundedRangeModel
     */
    void setIngestProgressListener(IngestProgressListener ingestProgressListener);
}