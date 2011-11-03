package org.escidoc.core.client.ingest.ws;


public class WebServiceFactory {

    private WebServiceFactory() {

    }

    static public WebServiceFactory getInstance() {
        return new WebServiceFactory();
    }

    public WebService getWebService(String type, String endpoint) {
        WebService ws = null;

        if (type.equalsIgnoreCase("GET")) {
            ws = new SimpleUrlWebService(endpoint);
        }
        // else if (type.equalsIgnoreCase("MULITPART")) {
        // ws = new MulitpartPartFormDataWebService(endpoint);
        // }
        else {
            throw new UnsupportedOperationException(
                "Unsupported webservice type \"" + type + "\".");
        }

        return ws;
    }
}
