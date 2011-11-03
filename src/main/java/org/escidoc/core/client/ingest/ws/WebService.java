package org.escidoc.core.client.ingest.ws;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Map;

import org.escidoc.core.client.ingest.ws.exceptions.ExecutionException;

/**
 * @author Frank Schwichtenberg
 * 
 */
public interface WebService {

    /**
     * Call the webservice defined by this instance.
     * 
     * @return The result returned by the webservice. The actual type depends on the mime-type.
     * 
     * @throws ExecutionException
     *             If the webservice reports an error.
     */
    public Object call() throws ExecutionException;

    /**
     * Add a parameter for the webservice call.
     * 
     * @param params
     *            Key-value pairs for sending as parameters to the webservice.
     * 
     * @throws NullPointerException
     *             If the specified key or value is null and the parameter map does not permit null keys or values.
     * @throws IllegalArgumentException
     *             If some property of the specified key or value prevents it from being stored as parameter.
     */
    public void addParam(String key, String value);

    /**
     * Add parameters for the webservice call. Key and value are tried to be handled as string.
     * 
     * @param params
     *            Key-value pairs for sending as parameters to the webservice.
     * 
     * @throws ClassCastException
     *             If the class of a key or value prevents it from being stored as parameter.
     * @throws NullPointerException
     *             If a key or value is null and the parameter map does not permit null keys or values.
     * @throws IllegalArgumentException
     *             If some property of a key or value prevents it from being stored as parameter.
     */
    public void addParams(Map<Object, Object> params);

    /**
     * Add a file as parameter for the webserivce call. The variables $name, $file, $localpath, $url are set for use in
     * string parameters.
     * 
     * @param file
     *            A which name, url, content, etc. should be included in the webservice call.
     * @throws MalformedURLException
     *             If no URL can be generated from the file.
     * 
     */
    public void addParams(File file) throws MalformedURLException;

    /**
     * Set the expected mime-type of the response.
     * 
     * @param mimeType
     *            A string representing a mime-type. E.g. text/plain, image/jpeg, etc..
     */
    public void setMimeType(String mimeType);
}
