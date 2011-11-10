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