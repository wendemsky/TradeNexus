package com.fidelity.models;

import java.math.BigDecimal;

public class Trade {
    private String instrumentId;
    private Integer quantity;
    private BigDecimal executionPrice;
    private String direction;
    private String clientId;
    private Order order;
    private String tradeId;
    private BigDecimal cashValue;

    public Trade(String instrumentId, Integer quantity, BigDecimal executionPrice, String direction, String clientId,
                 Order order, String tradeId, BigDecimal cashValue) {
        if (instrumentId == null || instrumentId.isEmpty()) {
            throw new IllegalArgumentException("instrumentId cannot be null or empty");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("quantity must be greater than 0");
        }
        if (executionPrice == null || executionPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("executionPrice must be greater than 0");
        }
        if (direction == null || direction.isEmpty()) {
            throw new IllegalArgumentException("direction cannot be null or empty");
        }
        if (clientId == null || clientId.isEmpty()) {
            throw new IllegalArgumentException("clientId cannot be null or empty");
        }
        if (tradeId == null || tradeId.isEmpty()) {
            throw new IllegalArgumentException("tradeId cannot be null or empty");
        }
        if (cashValue == null || cashValue.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("cashValue cannot be null and must be non-negative");
        }

        this.instrumentId = instrumentId;
        this.quantity = quantity;
        this.executionPrice = executionPrice;
        this.direction = direction;
        this.clientId = clientId;
        this.order = order;
        this.tradeId = tradeId;
        this.cashValue = cashValue;
    }

    public String getInstrumentId() {
        return instrumentId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getExecutionPrice() {
        return executionPrice;
    }

    public String getDirection() {
        return direction;
    }

    public String getClientId() {
        return clientId;
    }

    public Order getOrder() {
        return order;
    }

    public String getTradeId() {
        return tradeId;
    }

    public BigDecimal getCashValue() {
        return cashValue;
    }
}
