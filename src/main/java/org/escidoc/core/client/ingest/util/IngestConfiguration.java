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
package org.escidoc.core.client.ingest.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.escidoc.core.client.ingest.exceptions.ConfigurationException;
import org.escidoc.core.client.ingest.ws.WebService;
import org.escidoc.core.client.ingest.ws.WebServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IngestConfiguration {

    private static final String PROPERTIES_FILENAME = "escidoc-ingest-client.custom.properties";

    private static final String PROPERTIES_DEFAULT_FILENAME = "escidoc-ingest-client.properties";

    private static final String CATALINA_HOME = "catalina.home";

    private static final String PROPERTIES_BASEDIR = System.getProperty(CATALINA_HOME) + "/";

    private static final String PROPERTIES_DIR = PROPERTIES_BASEDIR + "conf/";

    private static final Logger LOG = LoggerFactory.getLogger(IngestConfiguration.class.getName());

    public static final String INGEST_PROPERTY_PREFIX = "escidoc.client.ingest.";

    private static IngestConfiguration instance = null;

    private final Properties properties;

    /**
     * Private Constructor, in order to prevent instantiation of this utility class. read the Properties and fill it in
     * properties attribute.
     * 
     * @throws ConfigurationException
     *             e
     * 
     */
    private IngestConfiguration() throws IOException {
        System.setProperty("java.awt.headless", "true");
        this.properties = loadProperties();
    }

    /**
     * Initializes and returns Configuration Object.
     * 
     * @return IngestConfiguration self
     * @throws IOException
     *             Thrown if properties loading fails.
     * 
     */
    public static synchronized IngestConfiguration getInstance() throws IOException {
        if (instance == null) {
            instance = new IngestConfiguration();
        }
        return instance;
    }

    /**
     * Returns the property with the given name or null if property was not found.
     * 
     * @param name
     *            The name of the Property.
     * @return Value of the given Property as String.
     */
    public String get(final String name) {
        return (String) properties.get(name);
    }

    /**
     * Returns the property with the given name or the second parameter as default value if property was not found.
     * 
     * @param name
     *            The name of the Property.
     * @param defaultValue
     *            The default vaule if property isn't given.
     * @return Value of the given Property as String.
     */
    public String get(final String name, final String defaultValue) {
        String prop = get(name);

        if (prop == null) {
            prop = defaultValue;
        }
        return prop;
    }

    /**
     * Loads the Properties from the possible files. First loads properties from the file escidoc-core.properties.
     * Afterwards tries to load specific properties from the file escidoc-core.custom.properties and merges them with
     * the default properties. If any key is included in default and specific properties, the value of the specific
     * property will overwrite the default property.
     * 
     * @return The properties
     * @throws ConfigurationException
     *             If the loading of the default properties (file escidoc-ingest-client.properties) fails.
     * 
     */
    private synchronized Properties loadProperties() throws IOException {
        Properties result;
        try {
            try {
                result = getProperties(PROPERTIES_DEFAULT_FILENAME);
            }
            catch (IOException e) {
                try {
                    result = getProperties(PROPERTIES_BASEDIR + PROPERTIES_DEFAULT_FILENAME);
                }
                catch (IOException e1) {
                    result = getProperties(PROPERTIES_DIR + PROPERTIES_DEFAULT_FILENAME);
                }
            }
        }
        catch (IOException e) {
            throw new IOException("Configuration file " + PROPERTIES_DEFAULT_FILENAME + " not found.");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Default properties: " + result);
        }
        Properties specific = null;
        try {
            specific = getProperties(PROPERTIES_FILENAME);
        }
        catch (IOException e) {
            try {
                specific = getProperties(PROPERTIES_BASEDIR + PROPERTIES_FILENAME);
            }
            catch (IOException e1) {
                try {
                    specific = getProperties(PROPERTIES_DIR + PROPERTIES_FILENAME);
                }
                catch (IOException e2) {
                    specific = new Properties();
                }
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Specific properties: " + specific);
        }
        result.putAll(specific);

        return result;
    }

    /**
     * Get the properties from a file.
     * 
     * @param filename
     *            The name of the properties file.
     * @return The properties.
     * @throws IOException
     *             If access to the specified file fails.
     */
    private synchronized Properties getProperties(final String filename) throws IOException {

        Properties result = new Properties();
        InputStream propertiesStream = getInputStream(filename);
        result.load(propertiesStream);
        return result;
    }

    /**
     * Get an InputStream for the given file.
     * 
     * @param filename
     *            The name of the file.
     * @return The InputStream or null if the file could not be located.
     * @throws FileNotFoundException
     *             If access to the specified file fails.
     */
    private synchronized InputStream getInputStream(final String filename) throws FileNotFoundException {

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filename);
        if (inputStream == null) {
            LOG.debug("Could not load config as resource. Trying to load file: " + filename + ".");
            inputStream = new FileInputStream(new File(filename));
        }
        return inputStream;
    }

    public static List<String> getInfrastructureUrls() {
        List<String> result = new Vector<String>();

        try {
            String urlsString = IngestConfiguration.getInstance().get(INGEST_PROPERTY_PREFIX + "infrastructure-urls");
            if (urlsString != null) {
                String[] urls = urlsString.split(",");
                for (String url : urls) {
                    if (url.trim().length() > 0) {
                        result.add(url.trim());
                    }
                }
            }
        }
        catch (IOException e) {
            // return empty list if no configuration file
            LOG.warn(e.getMessage());
        }

        return result;
    }

    public static Collection<WebService> getContentWebservices() throws ConfigurationException {

        Collection<WebService> result = new Vector<WebService>();

        try {
            String webserviceNames =
                IngestConfiguration.getInstance().get(IngestConfiguration.INGEST_PROPERTY_PREFIX + "ws.names.content");
            if (webserviceNames != null) {
                List<String> webserviceNameList = Arrays.asList(webserviceNames.split(","));
                Iterator<String> it = webserviceNameList.iterator();
                while (it.hasNext()) {
                    String webserviceName = it.next().trim();
                    if (webserviceName.length() != 0) {
                        String webservicePropertyPrefix =
                            IngestConfiguration.INGEST_PROPERTY_PREFIX + "ws." + webserviceName + ".";

                        WebService ws = null;
                        // type, endpoint, parameters, mime-type
                        String type = IngestConfiguration.getInstance().get(webservicePropertyPrefix + "type");
                        String endpoint = IngestConfiguration.getInstance().get(webservicePropertyPrefix + "endpoint");
                        ws = WebServiceFactory.getInstance().getWebService(type, endpoint);

                        // if parameters, set in webservice
                        String parameterString =
                            IngestConfiguration.getInstance().get(webservicePropertyPrefix + "parameters");
                        if (parameterString != null) {
                            List<String> parameterList = Arrays.asList(parameterString);
                            Iterator<String> parameterIt = parameterList.iterator();
                            while (parameterIt.hasNext()) {
                                String[] parameter = parameterIt.next().split("=");
                                String key = parameter[0].trim();
                                String value = parameter[1].trim();
                                if (key != null && key.length() > 0 && value != null && value.length() > 0) {
                                    ws.addParam(key, value);
                                }
                            }
                        }

                        // if 'mime-type' is set, set it in webservice
                        String mimeType = IngestConfiguration.getInstance().get(webservicePropertyPrefix + "mime-type");
                        if (mimeType != null) {
                            ws.setMimeType(mimeType);
                        }

                        result.add(ws);
                    }
                }
            }

        }
        catch (IOException e) {
            throw new ConfigurationException("Error accessing webservice configuration for content");
        }

        return result;
    }
}
