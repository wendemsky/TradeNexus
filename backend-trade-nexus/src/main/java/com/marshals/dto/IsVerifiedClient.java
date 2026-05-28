package com.marshals.dto;

public class IsVerifiedClient {

    private boolean isVerified;

    public IsVerifiedClient() {}

    public IsVerifiedClient(boolean isVerified) {
        this.isVerified = isVerified;
    }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }
}
