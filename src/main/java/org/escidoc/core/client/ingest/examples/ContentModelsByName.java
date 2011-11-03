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
package org.escidoc.core.client.ingest.examples;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.escidoc.core.client.ingest.Ingester;
import org.escidoc.core.client.ingest.byname.ByNameIngester;
import org.escidoc.core.client.ingest.entities.ResourceEntry;
import org.escidoc.core.client.ingest.exceptions.ConfigurationException;
import org.escidoc.core.client.ingest.exceptions.IngestException;

import de.escidoc.core.resources.ResourceType;

/**
 * Example of using {@link ByNameIngester} to ingest Content Models from a list
 * of known names.
 * 
 * @author Frank Schwichtenberg <Frank.Schwichtenberg@FIZ-Karlsruhe.de>
 * 
 */
public class ContentModelsByName {

    /**
     * @param args
     *            Names.
     * @throws IngestException
     *             See Ingester
     * @throws ConfigurationException
     *             See Ingester.
     * @throws IOException
     *             If Content Model IDs cannot be written to properties file.
     */
    public static void main(final String[] args) throws ConfigurationException,
        IngestException, IOException, MalformedURLException {

        URL infrastructureUrl = new URL(args[0]);
        String handle = args[1];

        String[] nameArray = Arrays.copyOfRange(args, 2, args.length);
        // { "Instrument Content Model", "Study Content Model",
        // "InvestigationSeries Content Model" };
        List<String> names = Arrays.asList(nameArray);

        Ingester ingester =
            new ByNameIngester(infrastructureUrl, handle, names,
                ResourceType.CONTENT_MODEL);

        ingester.ingest();

        System.out.println(((ByNameIngester) ingester).getObjectIdentifier());

        Properties contentModelProperties = new Properties();
        Iterator<ResourceEntry> it =
            ((ByNameIngester) ingester).getObjectIdentifier().iterator();
        while (it.hasNext()) {
            ResourceEntry re = it.next();
            contentModelProperties.setProperty(re.getTitle(),
                re.getIdentifier());
        }
        contentModelProperties.store(new FileOutputStream(
            "ContentModel.properties"), "Content Models for use with BW-eLabs");

    }

}
