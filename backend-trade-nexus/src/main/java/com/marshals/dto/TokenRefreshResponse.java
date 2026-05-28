package com.marshals.dto;

public class TokenRefreshResponse {

    private String token;
    private String expiresAt;

    public TokenRefreshResponse() {}

    public TokenRefreshResponse(String token, String expiresAt) {
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }
}
