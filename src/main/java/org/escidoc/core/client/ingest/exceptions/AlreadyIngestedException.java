package org.escidoc.core.client.ingest.exceptions;

/**
 * Indicates an entity is tried to ingest which is already ingested.
 * 
 * @author Frank Schwichtenberg <http://Frank.Schwichtenberg@FIZ-Karlsruhe.de>
 * 
 */
public class AlreadyIngestedException extends Exception {

    /**
	 * 
	 */
    private static final long serialVersionUID = 7812540079229294711L;

    public AlreadyIngestedException() {
        super();
        // TODO Auto-generated constructor stub
    }

    public AlreadyIngestedException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    public AlreadyIngestedException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    public AlreadyIngestedException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

}
