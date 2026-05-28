package com.marshals.exception;

public class PriceDataStaleException extends RuntimeException {
    public PriceDataStaleException(String message) {
        super(message);
    }
}
