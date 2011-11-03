package org.escidoc.core.client.ingest.model;

public interface IngestProgressListener {

    void setSum(int sum);

    void setIngested(int count);

    void incrementIngested();

    void incrementSum();
}
