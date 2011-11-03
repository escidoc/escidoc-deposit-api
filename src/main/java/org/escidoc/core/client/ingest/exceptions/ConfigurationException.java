/**
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at license/ESCIDOC.LICENSE
 * or https://www.escidoc.org/license/ESCIDOC.LICENSE .
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at license/ESCIDOC.LICENSE.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 *
 *
 * Copyright 2011 Fachinformationszentrum Karlsruhe Gesellschaft
 * fuer wissenschaftlich-technische Information mbH and Max-Planck-
 * Gesellschaft zur Foerderung der Wissenschaft e.V.
 * All rights reserved.  Use is subject to license terms.
 */
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
