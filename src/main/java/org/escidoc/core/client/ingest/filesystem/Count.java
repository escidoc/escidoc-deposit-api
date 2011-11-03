package org.escidoc.core.client.ingest.filesystem;

import org.escidoc.core.client.ingest.model.IngestProgressListener;

/**
 * A helper object to count what should be ingested. An
 * {@link IngestProgressListener} can be bound to be informed about the count.
 * 
 * @author Frank Schwichtenberg <http://Frank.Schwichtenberg@FIZ-Karlsruhe.de>
 * 
 */
public class Count {

    private int count = 0;

    private IngestProgressListener ingestProgressListener;

    public Count() {
        this.count = 0;
    }

    public Count(IngestProgressListener l) {
        this.ingestProgressListener = l;
        this.count = 0;
    }

    /**
     * Increment the internal counter.
     */
    synchronized public void increment() {
        this.count++;
        if (this.ingestProgressListener != null) {
            this.ingestProgressListener.incrementSum();
        }
    }

    /**
     * Get the value of the internal counter.
     * 
     * @return The current count.
     */
    public int getValue() {
        return this.count;
    }
}
