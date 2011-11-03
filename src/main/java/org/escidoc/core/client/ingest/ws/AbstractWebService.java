package org.escidoc.core.client.ingest.ws;

import java.io.File;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.escidoc.core.client.ingest.ws.exceptions.ExecutionException;

public abstract class AbstractWebService implements WebService {

    protected Map<String, String> params;

    protected File file;

    protected String mimeType = "application/octet-stream";

    protected AbstractWebService() {
        params = new HashMap<String, String>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.escidoc.core.client.ws.WebService#call()
     */
    @Override
    public abstract Object call() throws ExecutionException;

    /*
     * (non-Javadoc)
     * 
     * @see org.escidoc.core.client.ws.WebService#addParam(java.lang.String, java.lang.String)
     */
    @Override
    public void addParam(String key, String value) {
        this.params.put(key, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.escidoc.core.client.ws.WebService#addParams(java.util.Map)
     */
    @Override
    public void addParams(Map<Object, Object> params) {
        Iterator<Object> it = params.keySet().iterator();
        while (it.hasNext()) {
            Object o = it.next();
            this.addParam(o.toString(), params.get(o).toString());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.escidoc.core.client.ws.WebService#addParams(java.io.File)
     */
    @Override
    public void addParams(File file) throws MalformedURLException {
        this.file = file;
        this.params.put("localpath", file.getPath());
        this.params.put("name", file.getName());
        this.params.put("url", file.toURI().toURL().toString());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.escidoc.core.client.ws.WebService#setMimeType(java.lang.String)
     */
    @Override
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
