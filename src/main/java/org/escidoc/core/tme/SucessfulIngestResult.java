package org.escidoc.core.tme;

import com.google.common.base.Preconditions;

import org.w3c.dom.DOMException;

import de.escidoc.core.client.TransportProtocol;
import de.escidoc.core.client.exceptions.InternalClientException;
import de.escidoc.core.common.jibx.MarshallerFactory;
import de.escidoc.core.resources.common.Result;

public class SucessfulIngestResult implements IngestResult {

    private String resultAsXml;

    public SucessfulIngestResult(String resultAsXml) {
        Preconditions.checkNotNull(resultAsXml, "resultAsXml is null: %s", resultAsXml);
        this.resultAsXml = resultAsXml;
    }

    public String getId() throws DOMException, InternalClientException {
        return MarshallerFactory
            .getInstance(TransportProtocol.REST).getMarshaller(Result.class).unmarshalDocument(resultAsXml).getFirst()
            .getTextContent();
    }

    @Override
    public boolean isSuccesful() {
        return true;
    }
}
