package org.escidoc.core.client.ingest.exceptions;

/**
 * Indicating wrong or insufficient configuration.
 * 
 * @author Frank Schwichtenberg <http://Frank.Schwichtenberg@FIZ-Karlsruhe.de>
 * 
 */
public class ConfigurationException extends Exception {

    /**
	 * 
	 */
    private static final long serialVersionUID = -2107165353080572166L;

    public ConfigurationException() {
        super();
        // TODO Auto-generated constructor stub
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    public ConfigurationException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    public ConfigurationException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

}
