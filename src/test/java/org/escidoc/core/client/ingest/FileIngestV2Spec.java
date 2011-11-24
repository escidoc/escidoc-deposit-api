package org.escidoc.core.client.ingest;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
import java.net.URL;

import org.escidoc.core.tme.FileIngester;
import org.junit.Ignore;
import org.junit.Test;

import de.escidoc.core.client.Authentication;
import de.escidoc.core.resources.common.reference.ContentModelRef;
import de.escidoc.core.resources.common.reference.ContextRef;

public class FileIngestV2Spec {

    private static final String INPUT_FULL_PATH = "/home/chh/ingest-me/1.3/Rest_api_doc_OM_Item.1.3.pdf";

    // private static final String INPUT_FULL_PATH =
    // "/Users/bender/ingest-me/SWORD-Intro.pdf";

    private static final File fitsHome = new File("src/main/resources");

    private static final String SERVICE_URL = "http://esfedrep1.fiz-karlsruhe.de:8080/";

    private static final String SYSADMIN = "sysadmin";

    private static final String SYSADMIN_PASSWORD = "eSciDoc";

    private static final String CONTEXT_ID = "escidoc:136";

    private static final String ITEM_CONTENT_MODEL = "escidoc:12";

    @Ignore
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