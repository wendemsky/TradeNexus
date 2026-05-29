package com.marshals.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ResponseEntity<Map<String, Object>> error(int status, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "status", status,
                "message", message,
                "timestamp", OffsetDateTime.now().toString()
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException e) {
        return error(400, e.getMessage());
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<Map<String, Object>> handleBalance(InsufficientBalanceException e) {
        return error(402, e.getMessage());
    }

    @ExceptionHandler(MarketClosedException.class)
    public ResponseEntity<Map<String, Object>> handleMarketClosed(MarketClosedException e) {
        return error(409, e.getMessage());
    }

    @ExceptionHandler(LimitOrderNotMetException.class)
    public ResponseEntity<Map<String, Object>> handleLimitNotMet(LimitOrderNotMetException e) {
        return error(409, e.getMessage());
    }

    @ExceptionHandler(PriceDataStaleException.class)
    public ResponseEntity<Map<String, Object>> handleStalePrice(PriceDataStaleException e) {
        return error(503, e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleForbidden(AccessDeniedException e) {
        return error(403, e.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NoSuchElementException e) {
        return error(404, e.getMessage() != null ? e.getMessage() : "NOT_FOUND");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception e) {
        log.error("Unhandled exception", e);
        return error(500, "Unexpected error");
    }
}
