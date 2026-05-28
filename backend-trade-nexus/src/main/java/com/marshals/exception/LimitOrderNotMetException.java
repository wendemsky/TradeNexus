package com.marshals.exception;

public class LimitOrderNotMetException extends RuntimeException {
    public LimitOrderNotMetException(String message) {
        super(message);
    }
}
