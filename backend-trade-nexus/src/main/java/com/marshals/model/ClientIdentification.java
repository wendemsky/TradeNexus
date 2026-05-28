package com.marshals.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "client_identification")
public class ClientIdentification {

    @EmbeddedId
    private ClientIdentificationId id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("clientId")
    @JoinColumn(name = "client_id")
    private Client client;

    @Column(nullable = false)
    private String value;

    public ClientIdentification() {}

    public ClientIdentification(String clientId, String type, String value) {
        this.id = new ClientIdentificationId(clientId, type);
        this.value = value;
    }

    public ClientIdentificationId getId() { return id; }
    public void setId(ClientIdentificationId id) { this.id = id; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    @JsonProperty("type")
    public String getType() { return id != null ? id.getType() : null; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
