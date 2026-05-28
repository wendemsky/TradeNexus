package com.marshals.model;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class HoldingId implements Serializable {

    private String clientId;
    private String instrumentId;

    public HoldingId() {}

    public HoldingId(String clientId, String instrumentId) {
        this.clientId = clientId;
        this.instrumentId = instrumentId;
    }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getInstrumentId() { return instrumentId; }
    public void setInstrumentId(String instrumentId) { this.instrumentId = instrumentId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HoldingId that)) return false;
        return Objects.equals(clientId, that.clientId) && Objects.equals(instrumentId, that.instrumentId);
    }

    @Override
    public int hashCode() { return Objects.hash(clientId, instrumentId); }
}
