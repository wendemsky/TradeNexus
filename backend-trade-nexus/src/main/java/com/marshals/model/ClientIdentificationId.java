package com.marshals.model;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ClientIdentificationId implements Serializable {

    private String clientId;
    private String type;

    public ClientIdentificationId() {}

    public ClientIdentificationId(String clientId, String type) {
        this.clientId = clientId;
        this.type = type;
    }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientIdentificationId that)) return false;
        return Objects.equals(clientId, that.clientId) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() { return Objects.hash(clientId, type); }
}
