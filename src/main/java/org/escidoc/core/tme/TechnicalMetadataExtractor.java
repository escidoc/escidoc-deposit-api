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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.xml.sax.SAXException;

import com.google.common.base.Preconditions;

import edu.harvard.hul.ois.fits.Fits;
import edu.harvard.hul.ois.fits.exceptions.FitsException;

public class TechnicalMetadataExtractor {

    private File fitsHome;

    public TechnicalMetadataExtractor(File fitsHome) {
        Preconditions.checkNotNull(fitsHome, "fitsHome is null: %s", fitsHome);
        Preconditions.checkArgument(fitsHome.exists(), fitsHome + " does not exists");
        this.fitsHome = fitsHome;
    }

    public org.w3c.dom.Element extract(final File source) throws FitsException, SAXException, IOException,
        ParserConfigurationException {

        Format format = Format.getPrettyFormat();
        format.setOmitDeclaration(true);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        return factory
            .newDocumentBuilder()
            .parse(
                new ByteArrayInputStream(new XMLOutputter(format).outputString(
                    new Fits(fitsHome.getAbsolutePath()).examine(source).getFitsXml()).getBytes()))
            .getDocumentElement();
    }
}