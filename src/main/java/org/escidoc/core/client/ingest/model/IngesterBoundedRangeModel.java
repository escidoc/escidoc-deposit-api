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
    synchronized public void incrementIngested() {
        this.setValue(this.getValue() + 1);
        System.out.println("" + getValue() + "/" + getMaximum());
    }

}
