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

import java.io.File;
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

    protected File fitsHome;

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

        ContentModelHandlerClientInterface cmc = new ContentModelHandlerClient(eSciDocInfrastructureBaseUrl);
        cmc.setHandle(userHandle);

        SearchRetrieveRequestType request = new SearchRetrieveRequestType();
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

        ContextHandlerClientInterface cc = new ContextHandlerClient(eSciDocInfrastructureBaseUrl);
        cc.setHandle(userHandle);

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

        if (getMimeType() != null && !result.contains(getMimeType())) {
            result.add(0, getMimeType());
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

        if (getInitialLifecycleStatus() != null && !result.contains(getInitialLifecycleStatus())) {
            result.remove(getInitialLifecycleStatus());
            result.add(0, getInitialLifecycleStatus());
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
            setIngestStarted(true);
            checkConfiguration();
            ingestHook();
        }
        catch (ConfigurationException e) {
            setIngestStarted(false);
            throw e;
        }
        catch (IngestException e) {
            setIngestStarted(false);
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

        if (eSciDocInfrastructureBaseUrl == null) {
            throw new ConfigurationException("eSciDocInfrastructureBaseUrl must be set.");
        }

        if (userHandle == null || userHandle.trim().length() == 0) {
            throw new ConfigurationException("userHandle must be set.");
        }

        if (context == null || context.trim().length() == 0) {
            throw new ConfigurationException("Context must be set.");
        }

        if (containerContentModel == null || containerContentModel.trim().length() == 0) {
            throw new ConfigurationException("containerContentModel must be set.");
        }

        if (itemContentModel == null || itemContentModel.trim().length() == 0) {
            throw new ConfigurationException("itemContentModel must be set.");
        }

        if (contentCategory == null || contentCategory.trim().length() == 0) {
            throw new ConfigurationException("contentCategory must be set.");
        }

        if (initialLifecycleStatus == null) {
            throw new ConfigurationException("initialLifecycleStatus must be set.");
        }

        if (mimeType == null || mimeType.trim().length() == 0) {
            throw new ConfigurationException("Mime-Type must be set.");
        }

        if (visibility == null || visibility.trim().length() == 0) {
            throw new ConfigurationException("Content visibility must be set.");
        }

        if (validStatus == null || validStatus.trim().length() == 0) {
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
        String string = IngestConfiguration.getInstance().get(IngestConfiguration.INGEST_PROPERTY_PREFIX + "fits-home");
        this.fitsHome =
            new File(string);
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
        if (ingestHandler == null) {
            ingestHandler = new IngestHandlerClient(geteSciDocInfrastructureBaseUrl());
            ingestHandler.setHandle(getUserHandle());
        }
        return ingestHandler;
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
        if (containerHandler == null) {
            containerHandler = new ContainerHandlerClient(geteSciDocInfrastructureBaseUrl());
            containerHandler.setHandle(getUserHandle());
        }
        return containerHandler;
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
        if (itemHandler == null) {
            itemHandler = new ItemHandlerClient(geteSciDocInfrastructureBaseUrl());
            itemHandler.setHandle(getUserHandle());
        }
        return itemHandler;
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
        if (contentModelHandler == null) {
            contentModelHandler = new ContentModelHandlerClient(geteSciDocInfrastructureBaseUrl());
            contentModelHandler.setHandle(getUserHandle());
        }
        return contentModelHandler;
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
        if (stagingHandler == null) {
            stagingHandler = new StagingHandlerClient(geteSciDocInfrastructureBaseUrl());
            stagingHandler.setHandle(getUserHandle());
        }
        return stagingHandler;
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
        if (ingestStarted && isIngestStarted()) {
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
        if (contentCategory == null) {
            contentCategory = "ORIGINAL";
        }
        return contentCategory;
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

    @Override
    public File getFitsHome() {
        return fitsHome;
    }

    @Override
    public void setFitsHome(File fitsHome) {
        this.fitsHome = fitsHome;
    }

}