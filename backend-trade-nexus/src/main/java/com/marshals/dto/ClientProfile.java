package com.marshals.dto;

import com.marshals.model.Client;

public class ClientProfile {

    private Client client;
    private String token;

    public ClientProfile() {}

    public ClientProfile(Client client, String token) {
        this.client = client;
        this.token = token;
    }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
