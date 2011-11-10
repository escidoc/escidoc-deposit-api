package org.escidoc.core.client.tme;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.escidoc.core.tme.FileIngester;
import org.escidoc.core.tme.TechnicalMetadataExtractor;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.junit.Ignore;
import org.junit.Test;

import de.escidoc.core.client.Authentication;
import de.escidoc.core.resources.common.reference.ContentModelRef;
import de.escidoc.core.resources.common.reference.ContextRef;
import edu.harvard.hul.ois.fits.Fits;
import edu.harvard.hul.ois.fits.FitsMetadataElement;
import edu.harvard.hul.ois.fits.FitsOutput;

public class TmeSpec {

    private static final String INPUT_FULL_PATH = "/home/chh/ingest-me/1.3/Rest_api_doc_OM_Item.1.3.pdf";

    // private static final String INPUT_FULL_PATH =
    // "/Users/bender/ingest-me/SWORD-Intro.pdf";

    private static final File fitsHome = new File("src/main/resources");

    private static final String FITS_HOME = "src/test/resources";

    private static final String SERVICE_URL = "http://esfedrep1.fiz-karlsruhe.de:8080/";

    private static final String SYSADMIN = "sysadmin";

    private static final String SYSADMIN_PASSWORD = "eSciDoc";

    private static final String CONTEXT_ID = "escidoc:136";

    private static final String ITEM_CONTENT_MODEL = "escidoc:12";

    @Ignore
    @Test
    public void foo() throws Exception {
        // Given
        final Fits fits = new Fits(FITS_HOME);
        // When

        FitsOutput output = fits.examine(new File(INPUT_FULL_PATH));
        final Document doc = output.getFitsXml();
        final XMLOutputter serializer = new XMLOutputter(Format.getPrettyFormat());
        serializer.output(doc, System.out);

        final Element rootElement = doc.getRootElement();
        System.out.println(rootElement);
        // AssertThat:
        final List<FitsMetadataElement> techMetadataElements = output.getTechMetadataElements();
        assertTrue(!techMetadataElements.isEmpty());
    }

    @Ignore
    @Test
    public void bar() throws Exception {
        // Given X0 && ...Xn
        final File input = new File(INPUT_FULL_PATH);
        final InputStream is = new FileInputStream(input);
        final TechnicalMetadataExtractor me = new TechnicalMetadataExtractor(fitsHome);
        // When
        final org.w3c.dom.Element element = me.extract(input);
        // Then ensure that

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(element), new StreamResult(writer));
        System.out.print(writer.toString());
    }

    // @Ignore
    @Test
    public void shouldIngestFileAndItsTechnicalMetadata() throws Exception {
        // Given:
        Authentication authentication = new Authentication(new URL(SERVICE_URL), SYSADMIN, SYSADMIN_PASSWORD);
        ContextRef contextRef = new ContextRef(CONTEXT_ID);
        ContentModelRef contentModelRef = new ContentModelRef(ITEM_CONTENT_MODEL);
        String userHandle = authentication.getHandle();
        File source = new File(INPUT_FULL_PATH);
        URI serviceUri = new URI(SERVICE_URL);

        // When:
        FileIngester ingester = new FileIngester(source, contextRef, contentModelRef, serviceUri, userHandle, fitsHome);
        String result = ingester.ingest();

        // AssertThat:
        assertTrue(!result.isEmpty());
        System.out.println("result" + result);
    }
}