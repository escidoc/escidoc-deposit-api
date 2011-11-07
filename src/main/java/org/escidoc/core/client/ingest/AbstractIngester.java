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

import gov.loc.www.zing.srw.SearchRetrieveRequestType;

import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.escidoc.core.client.ingest.entities.ResourceEntry;
import org.escidoc.core.client.ingest.exceptions.ConfigurationException;
import org.escidoc.core.client.ingest.exceptions.IngestException;
import org.escidoc.core.client.ingest.model.IngestProgressListener;
import org.escidoc.core.client.ingest.util.IngestConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.escidoc.core.client.ContainerHandlerClient;
import de.escidoc.core.client.ContentModelHandlerClient;
import de.escidoc.core.client.ContextHandlerClient;
import de.escidoc.core.client.IngestHandlerClient;
import de.escidoc.core.client.ItemHandlerClient;
import de.escidoc.core.client.StagingHandlerClient;
import de.escidoc.core.client.TransportProtocol;
import de.escidoc.core.client.exceptions.EscidocException;
import de.escidoc.core.client.exceptions.InternalClientException;
import de.escidoc.core.client.exceptions.TransportException;
import de.escidoc.core.client.interfaces.ContainerHandlerClientInterface;
import de.escidoc.core.client.interfaces.ContentModelHandlerClientInterface;
import de.escidoc.core.client.interfaces.ContextHandlerClientInterface;
import de.escidoc.core.client.interfaces.IngestHandlerClientInterface;
import de.escidoc.core.client.interfaces.ItemHandlerClientInterface;
import de.escidoc.core.client.interfaces.StagingHandlerClientInterface;
import de.escidoc.core.resources.cmm.ContentModel;
import de.escidoc.core.resources.common.properties.PublicStatus;
import de.escidoc.core.resources.om.context.Context;

/**
 * Abstract implementation of <code>org.escidoc.client.ignest.Ingester</code>. Provides an abstract hook method which
 * must be overridden specifying the concrete ingest procedure.
 * 
 * @see org.escidoc.core.client.ingest.DefaultIngester
 * 
 * @author Frank Schwichtenberg <Frank.Schwichtenberg@FIZ-Karlsruhe.de>
 * 
 */
public abstract class AbstractIngester implements Ingester {

    private String userHandle;

    private URL eSciDocInfrastructureBaseUrl;

    private boolean ingestStarted = false;

    private String context;

    private String containerContentModel;

    private String itemContentModel;

    private String contentCategory;

    private PublicStatus initialLifecycleStatus;

    private String mimeType;

    private String visibility;

    private String validStatus;

    protected IngestProgressListener ingestProgressListener;

    private IngestHandlerClientInterface ingestHandler;

    private ContainerHandlerClientInterface containerHandler;

    private ItemHandlerClientInterface itemHandler;

    private StagingHandlerClientInterface stagingHandler;

    private ContentModelHandlerClientInterface contentModelHandler;

    public boolean isCanceled = false;

    private static final Logger LOG = LoggerFactory.getLogger(AbstractIngester.class.getName());

    /**
     * Scince the eSciDoc Infrastructure 1.2.x does not support lists of Content Model this method returns the
     * configured default Content Models.
     * 
     * @see org.escidoc.core.client.ingest.Ingester#getContentModels()
     */
    @Override
    public final List<ResourceEntry> getContentModels() throws EscidocException, InternalClientException,
        TransportException {
        List<ResourceEntry> result = new Vector<ResourceEntry>();

        ContentModelHandlerClientInterface cmc = new ContentModelHandlerClient(this.eSciDocInfrastructureBaseUrl);
        cmc.setHandle(this.userHandle);
        cmc.setTransport(TransportProtocol.REST);

        SearchRetrieveRequestType request = new SearchRetrieveRequestType();
        // request.setQuery("type = context");
        Collection<ContentModel> contextList = cmc.retrieveContentModelsAsList(request);
        Iterator<ContentModel> contextIt = contextList.iterator();
        while (contextIt.hasNext()) {
            ContentModel context = contextIt.next();
            ResourceEntry re = new ResourceEntry();

            re.setTitle(context.getProperties().getName());
            re.setIdentifier(context.getObjid());
            re.setHref(context.getXLinkHref());

            result.add(re);
        }

        // ContentModel ccm = cmc.retrieve(this.containerContentModel);
        // ResourceEntry ccmre = new ResourceEntry();
        // ccmre.setTitle(ccm.getProperties().getName());
        // ccmre.setIdentifier(ccm.getObjid());
        // ccmre.setHref(ccm.getXLinkHref());
        // result.add(ccmre);
        //
        // if (this.itemContentModel != null) {
        // ContentModel icm = cmc.retrieve(this.itemContentModel);
        // ResourceEntry icmre = new ResourceEntry();
        // icmre.setTitle(icm.getProperties().getName());
        // icmre.setIdentifier(icm.getObjid());
        // icmre.setHref(icm.getXLinkHref());
        // result.add(icmre);
        // }

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.escidoc.core.client.ingest.Ingester#getContexts()
     */
    @Override
    public final List<ResourceEntry> getContexts() throws InternalClientException, EscidocException, TransportException {
        List<ResourceEntry> result = new Vector<ResourceEntry>();

        ContextHandlerClientInterface cc = new ContextHandlerClient(this.eSciDocInfrastructureBaseUrl);
        cc.setHandle(this.userHandle);
        cc.setTransport(TransportProtocol.REST);

        SearchRetrieveRequestType request = new SearchRetrieveRequestType();
        request.setQuery("type = context");
        Collection<Context> contextList = cc.retrieveContextsAsList(request);
        Iterator<Context> contextIt = contextList.iterator();
        while (contextIt.hasNext()) {
            Context context = contextIt.next();
            ResourceEntry re = new ResourceEntry();

            re.setTitle(context.getProperties().getName());
            re.setIdentifier(context.getObjid());
            re.setHref(context.getXLinkHref());

            result.add(re);
        }

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.escidoc.core.client.ingest.Ingester#getMimeTypes()
     */
    @Override
    public List<String> getMimeTypes() {
        List<String> result = new Vector<String>();

        result.add("text/xml");
        result.add("image/jpeg");
        result.add("image/gif");
        result.add("image/tiff");
        result.add("image/png");
        result.add("application/pdf");
        result.add("application/octet-stream");

        if (this.getMimeType() != null && !result.contains(this.getMimeType())) {
            result.add(0, this.getMimeType());
        }

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.escidoc.core.client.ingest.Ingester#getLifecycleStatus()
     */
    @Override
    public List<PublicStatus> getLifecycleStatus() {
        List<PublicStatus> result = new Vector<PublicStatus>();
        result.add(PublicStatus.PENDING);
        result.add(PublicStatus.SUBMITTED);
        result.add(PublicStatus.RELEASED);

        if (this.getInitialLifecycleStatus() != null && !result.contains(this.getInitialLifecycleStatus())) {
            result.remove(this.getInitialLifecycleStatus());
            result.add(0, this.getInitialLifecycleStatus());
        }

        return result;
    }

    /**
     * Template method for ingest. Use <code>ingestHook()</code> to implement your ingest procedure.
     * 
     * @see org.escidoc.core.client.ingest.Ingester#ingest()
     */
    @Override
    public final void ingest() throws ConfigurationException, IngestException {

        try {

            this.setIngestStarted(true);
            this.checkConfiguration();
            ingestHook();

        }
        catch (ConfigurationException e) {
            this.setIngestStarted(false);
            throw e;
        }
        catch (IngestException e) {
            this.setIngestStarted(false);
            throw e;
        }
    }

    /**
     * 
     * @throws ConfigurationException
     *             If this Ingester is not sufficient configured.
     * @throws IngestException
     *             If an error occurs while ingesting.
     */
    protected abstract void ingestHook() throws ConfigurationException, IngestException;

    /**
     * Checks if the Ingester is sufficient configured.
     * 
     * @throws ConfigurationException
     *             If this Ingester is not sufficient configured.
     */
    public void checkConfiguration() throws ConfigurationException {

        if (this.eSciDocInfrastructureBaseUrl == null) {
            throw new ConfigurationException("eSciDocInfrastructureBaseUrl must be set.");
        }

        if (this.userHandle == null || this.userHandle.trim().length() == 0) {
            throw new ConfigurationException("userHandle must be set.");
        }

        if (this.context == null || this.context.trim().length() == 0) {
            throw new ConfigurationException("Context must be set.");
        }

        if (this.containerContentModel == null || this.containerContentModel.trim().length() == 0) {
            throw new ConfigurationException("containerContentModel must be set.");
        }

        if (this.itemContentModel == null || this.itemContentModel.trim().length() == 0) {
            throw new ConfigurationException("itemContentModel must be set.");
        }

        if (this.contentCategory == null || this.contentCategory.trim().length() == 0) {
            throw new ConfigurationException("contentCategory must be set.");
        }

        if (this.initialLifecycleStatus == null) {
            throw new ConfigurationException("initialLifecycleStatus must be set.");
        }

        if (this.mimeType == null || this.mimeType.trim().length() == 0) {
            throw new ConfigurationException("Mime-Type must be set.");
        }

        if (this.visibility == null || this.visibility.trim().length() == 0) {
            throw new ConfigurationException("Content visibility must be set.");
        }

        if (this.validStatus == null || this.validStatus.trim().length() == 0) {
            throw new ConfigurationException("Content valid status must be set.");
        }

    }

    /**
     * Loads values from ingester configuration.
     */
    protected void loadConfiguration() {

        this.context = IngestConfiguration.getInstance().get(IngestConfiguration.INGEST_PROPERTY_PREFIX + "context");
        this.containerContentModel =
            IngestConfiguration.getInstance().get(
                IngestConfiguration.INGEST_PROPERTY_PREFIX + "content-model.container");
        this.itemContentModel =
            IngestConfiguration.getInstance().get(IngestConfiguration.INGEST_PROPERTY_PREFIX + "content-model.item");
        this.initialLifecycleStatus = PublicStatus.PENDING;
        // IngestConfiguration.getInstance().get(
        // IngestConfiguration.INGEST_PROPERTY_PREFIX + "status");
        this.contentCategory =
            IngestConfiguration.getInstance().get(IngestConfiguration.INGEST_PROPERTY_PREFIX + "content-category");
        this.mimeType = IngestConfiguration.getInstance().get(IngestConfiguration.INGEST_PROPERTY_PREFIX + "mime-type");
        this.visibility =
            IngestConfiguration.getInstance().get(IngestConfiguration.INGEST_PROPERTY_PREFIX + "visibility");
        this.validStatus =
            IngestConfiguration.getInstance().get(IngestConfiguration.INGEST_PROPERTY_PREFIX + "valid-status");
    }

    // eSciDoc Infrastructure Client Handler

    /**
     * Returns a handler client object in order to access the underlying eSciDoc Infrastructure.
     * 
     * @return The IngestHandlerClient of this Ingester.
     * @throws InternalClientException
     *             If an internal error in the eSciDoc Client Library occurs.
     */
    protected final IngestHandlerClientInterface getIngestHandlerClient() throws InternalClientException {
        // Ingest Handler
        if (this.ingestHandler == null) {
            this.ingestHandler = new IngestHandlerClient(this.geteSciDocInfrastructureBaseUrl());
            this.ingestHandler.setHandle(this.getUserHandle());
            this.ingestHandler.setTransport(TransportProtocol.REST);
        }
        return this.ingestHandler;
    }

    /**
     * Returns a handler client object in order to access the underlying eSciDoc Infrastructure.
     * 
     * @return The ContainerHandlerClient of this Ingester.
     * @throws InternalClientException
     *             If an internal error in the eSciDoc Client Library occurs.
     */
    protected final ContainerHandlerClientInterface getContainerHandlerClient() throws InternalClientException {
        // Container Handler
        if (this.containerHandler == null) {
            this.containerHandler = new ContainerHandlerClient(this.geteSciDocInfrastructureBaseUrl());
            this.containerHandler.setHandle(this.getUserHandle());
            this.containerHandler.setTransport(TransportProtocol.REST);
        }
        return this.containerHandler;
    }

    /**
     * Returns a handler client object in order to access the underlying eSciDoc Infrastructure.
     * 
     * @return The ItemHandlerClient of this Ingester.
     * @throws InternalClientException
     *             If an internal error in the eSciDoc Client Library occurs.
     */
    protected final ItemHandlerClientInterface getItemHandlerClient() throws InternalClientException {
        // Item Handler
        if (this.itemHandler == null) {
            this.itemHandler = new ItemHandlerClient(this.geteSciDocInfrastructureBaseUrl());
            this.itemHandler.setHandle(this.getUserHandle());
            this.itemHandler.setTransport(TransportProtocol.REST);
        }
        return this.itemHandler;
    }

    /**
     * Returns a handler client object in order to access the underlying eSciDoc Infrastructure.
     * 
     * @return The ContentModelHandlerClientInterface of this Ingester.
     * @throws InternalClientException
     *             If an internal error in the eSciDoc Client Library occurs.
     */
    protected final ContentModelHandlerClientInterface getContentModelHandlerClient() throws InternalClientException {
        // Item Handler
        if (this.contentModelHandler == null) {
            this.contentModelHandler = new ContentModelHandlerClient(this.geteSciDocInfrastructureBaseUrl());
            this.contentModelHandler.setHandle(this.getUserHandle());
            this.contentModelHandler.setTransport(TransportProtocol.REST);
        }
        return this.contentModelHandler;
    }

    /**
     * Returns a handler client object in order to access the underlying eSciDoc Infrastructure.
     * 
     * @return The StagingHandlerClient of this Ingester.
     * @throws InternalClientException
     *             If an internal error in the eSciDoc Client Library occurs.
     */
    protected final StagingHandlerClientInterface getStagingHandlerClient() throws InternalClientException {
        // Staging Handler
        if (this.stagingHandler == null) {
            this.stagingHandler = new StagingHandlerClient(this.geteSciDocInfrastructureBaseUrl());
            this.stagingHandler.setHandle(this.getUserHandle());
            this.stagingHandler.setTransport(TransportProtocol.REST);
        }
        return this.stagingHandler;
    }

    // getter/setter

    protected final String getUserHandle() {
        return userHandle;
    }

    protected final void setUserHandle(String userHandle) throws ConfigurationException {
        if (this.userHandle != null) {
            throw new ConfigurationException("User handle must not be changed.");
        }
        this.userHandle = userHandle;
    }

    protected final URL geteSciDocInfrastructureBaseUrl() {
        return eSciDocInfrastructureBaseUrl;
    }

    protected final void seteSciDocInfrastructureBaseUrl(final URL eSciDocInfrastructureBaseUrl)
        throws ConfigurationException {
        if (this.eSciDocInfrastructureBaseUrl != null) {
            throw new ConfigurationException("The URL of the underlying eSciDoc Infrastructure must not be changed.");
        }
        this.eSciDocInfrastructureBaseUrl = eSciDocInfrastructureBaseUrl;
    }

    private boolean isIngestStarted() {
        return ingestStarted;
    }

    protected final void setIngestStarted(boolean ingestStarted) throws ConfigurationException {
        if (ingestStarted && this.isIngestStarted()) {
            throw new ConfigurationException("Ingest was already started.");
        }
        this.ingestStarted = ingestStarted;
    }

    @Override
    public final String getContext() {
        return context;
    }

    @Override
    public final void setContext(String context) {
        this.context = context;
    }

    @Override
    public final String getContainerContentModel() {
        return containerContentModel;
    }

    @Override
    public final void setContainerContentModel(String containerContentModel) {
        this.containerContentModel = containerContentModel;
    }

    @Override
    public final String getItemContentModel() {
        return itemContentModel;
    }

    @Override
    public final void setItemContentModel(String itemContentModel) {
        this.itemContentModel = itemContentModel;
    }

    @Override
    public final String getContentCategory() {
        if (this.contentCategory == null) {
            this.contentCategory = "ORIGINAL";
        }
        return this.contentCategory;
    }

    @Override
    public final void setContentCategory(String contentCategory) {
        this.contentCategory = contentCategory;
    }

    @Override
    public final PublicStatus getInitialLifecycleStatus() {
        return initialLifecycleStatus;
    }

    @Override
    public final void setInitialLifecycleStatus(PublicStatus initialLifecycleStatus) {
        this.initialLifecycleStatus = initialLifecycleStatus;
    }

    @Override
    public final String getMimeType() {
        return mimeType;
    }

    @Override
    public final void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public final String getVisibility() {
        return visibility;
    }

    @Override
    public final void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    @Override
    public final String getValidStatus() {
        return validStatus;
    }

    @Override
    public final void setValidStatus(String validStatus) {
        this.validStatus = validStatus;
    }

    @Override
    public final void setIngestProgressListener(IngestProgressListener ingestProgressListener) {
        this.ingestProgressListener = ingestProgressListener;
    }
}
