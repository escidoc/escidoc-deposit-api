package org.escidoc.core.tme;

import java.io.File;

public class FailIngest implements IngestResult {

    private File source;

    private Exception e;

    public FailIngest(File source, Exception e) {
        this.source = source;
        this.e = e;
    }

    @Override
    public boolean isSuccesful() {
        return false;
    }
}