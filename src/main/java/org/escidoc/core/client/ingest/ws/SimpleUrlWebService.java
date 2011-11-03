package org.escidoc.core.client.ingest.ws;

import org.escidoc.core.client.ingest.ws.exceptions.ExecutionException;

public class SimpleUrlWebService extends AbstractWebService {

    private String urlString;

    /**
     * Create a WebService instance for HTTP GET requests. Variables (words begining with '$') in the URL are replaced
     * by values with the variable name as key from given parameters.
     * 
     * @param url
     *            The URL to make a HTTP GET request to.
     */
    public SimpleUrlWebService(String url) {
        super();
        this.urlString = url;
    }

    @Override
    public Object call() throws ExecutionException {

        // // try to replace variables in parameters
        // Iterator<String> it = this.params.keySet().iterator();
        // while (it.hasNext()) {
        // String key = it.next();
        // String value = this.params.get(key);
        // if (value.startsWith("$")) {
        // this.params.put(key, this.params.get(value.substring(1)));
        // }
        // }
        //
        // // try to replace variables in url from parameters
        // it = this.params.keySet().iterator();
        // while (it.hasNext()) {
        // String var = it.next();
        // this.urlString = this.urlString.replaceAll("\\$" + var, this.params.get(var));
        // }
        //
        // // create query string from parameters
        // String query = "";
        // it = this.params.keySet().iterator();
        // while (it.hasNext()) {
        // String key = it.next().trim();
        // String value = this.params.get(key).trim();
        // if (key != null && key.length() > 0 && value != null && value.length() > 0) {
        // if (query.length() > 0) {
        // query += "&";
        // }
        // query += key + "=" + value;
        // }
        // }
        //
        // // FIXME improve http client handling
        // HttpClient client = new HttpClient();
        // GetMethod get = new GetMethod(urlString);
        // // TODO correct?
        // get.setQueryString("?" + query);
        // try {
        // System.out.println("execute");
        // client.executeMethod(get);
        // System.out.println("execute DONE");
        // }
        // catch (Exception e) {
        // throw new ExecutionException(e);
        // }
        // int statusCode = get.getStatusCode();
        // String charSet = get.getResponseCharSet();
        // String contentType = get.getResponseHeader("Content-Type").getValue();
        // System.out.println(get.getStatusLine() + ", " + charSet + ", " + contentType);
        //
        // if (statusCode / 100 != 2) {
        // throw new ExecutionException("Request failed. " + get.getStatusLine() + ", " + get.getStatusText());
        // }
        //
        // Object result = handleResponse(get);
        //
        // return result;
        return null;
    }

    // private Object handleResponse(HttpMethod method) throws ExecutionException {
    // Object result = null;
    //
    // try {
    // if (this.mimeType.equalsIgnoreCase("text/xml")) {
    //
    // try {
    // InputStream resultStream = method.getResponseBodyAsStream();
    // DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    // DocumentBuilder db;
    // try {
    // db = dbf.newDocumentBuilder();
    // }
    // catch (ParserConfigurationException e) {
    // // FIXME what kind of exception
    // throw new RuntimeException(e);
    // }
    // result = db.parse(resultStream);
    // method.releaseConnection();
    // }
    // catch (IOException e) {
    // throw new ExecutionException("Error reading response body.", e);
    // }
    // catch (SAXException e) {
    // throw new ExecutionException("Error parsing response body.", e);
    // }
    // }
    // else if (this.mimeType.startsWith("text/")) {
    // try {
    // result = method.getResponseBodyAsString();
    // }
    // catch (IOException e) {
    // throw new ExecutionException("Error reading response body.");
    // }
    // }
    // else {
    // // for all other mime-types return stream
    // try {
    // result = method.getResponseBodyAsStream();
    // }
    // catch (IOException e) {
    // throw new ExecutionException("Error reading response body.");
    // }
    // }
    // }
    // catch (ExecutionException e) {
    // method.releaseConnection();
    // throw e;
    // }
    //
    // return result;
    // }

}
