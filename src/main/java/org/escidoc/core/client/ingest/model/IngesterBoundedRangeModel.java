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
package org.escidoc.core.client.ingest.model;

import javax.swing.DefaultBoundedRangeModel;

public class IngesterBoundedRangeModel extends DefaultBoundedRangeModel
		implements IngestProgressListener {

	/**
     * 
     */
	private static final long serialVersionUID = 5514281738014214279L;

	public IngesterBoundedRangeModel() {
		super(0, 0, 0, 0);
		System.out.println("" + getValue() + "/" + getMaximum());
	}

	public IngesterBoundedRangeModel(int value, int extent, int min, int max) {
		super(value, extent, min, max);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setSum(int sum) {
		this.setMaximum(sum);
		System.out.println("" + getValue() + "/" + getMaximum());
	}

	@Override
	synchronized public void incrementSum() {
		this.setMaximum(this.getMaximum() + 1);
		System.out.println("" + getValue() + "/" + getMaximum());
	}

	@Override
	public void setIngested(int count) {
		this.setValue(count);
		System.out.println("" + getValue() + "/" + getMaximum());
	}

	@Override
	public int getIngested() {
		return this.getValue();
	}

	@Override
	synchronized public void incrementIngested() {
		this.setValue(this.getValue() + 1);
		System.out.println("" + getValue() + "/" + getMaximum());
	}

}
