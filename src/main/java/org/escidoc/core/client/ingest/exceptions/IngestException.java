package org.escidoc.core.client.ingest.exceptions;

/**
 * Indicates an error while ingesting.
 * 
 * @author Frank Schwichtenberg <http://Frank.Schwichtenberg@FIZ-Karlsruhe.de>
 * 
 */
public class IngestException extends Exception {

    /**
	 * 
	 */
    private static final long serialVersionUID = -597908879668354852L;

    public IngestException() {
        // TODO Auto-generated constructor stub
    }

    public IngestException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    public IngestException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

    public IngestException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

}
