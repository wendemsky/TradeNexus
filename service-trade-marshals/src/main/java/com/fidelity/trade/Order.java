package com.fidelity.trade;

import java.math.BigDecimal;

public class Order {
    private String instrumentId;
    private Integer quantity;
    private BigDecimal targetPrice;
    private String direction;
    private String clientId;
    private String orderId;
    private Integer token;

    public Order(String instrumentId, Integer quantity, BigDecimal targetPrice, String direction, String clientId,
                 String orderId, Integer token) {
        if (instrumentId == null || instrumentId.isEmpty()) {
            throw new IllegalArgumentException("instrumentId cannot be null or empty");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("quantity must be greater than 0");
        }
        if (targetPrice == null || targetPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("targetPrice must be greater than 0");
        }
        if (direction == null || direction.isEmpty()) {
            throw new IllegalArgumentException("direction cannot be null or empty");
        }
        if (clientId == null || clientId.isEmpty()) {
            throw new IllegalArgumentException("clientId cannot be null or empty");
        }
        if (orderId == null || orderId.isEmpty()) {
            throw new IllegalArgumentException("orderId cannot be null or empty");
        }
        if (token == null || token <= 0) {
            throw new IllegalArgumentException("token must be greater than 0");
        }

        this.instrumentId = instrumentId;
        this.quantity = quantity;
        this.targetPrice = targetPrice;
        this.direction = direction;
        this.clientId = clientId;
        this.orderId = orderId;
        this.token = token;
    }

    public String getInstrumentId() {
        return instrumentId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getTargetPrice() {
        return targetPrice;
    }

    public String getDirection() {
        return direction;
    }

    public String getClientId() {
        return clientId;
    }

    public String getOrderId() {
        return orderId;
    }

    public Integer getToken() {
        return token;
    }
}

