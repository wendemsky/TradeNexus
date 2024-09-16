package com.fidelity.clientportfolio;

public class Trade {
	private String instrumentId;
    private int quantity;
    private double executionPrice;
    private String direction;
    private String clientId;
    private Order order;
    private String tradeId;
    private double cashValue;

    // Constructor
    public Trade(String instrumentId, int quantity, double executionPrice, String direction, String clientId,
                 Order order, String tradeId, double cashValue) {
        this.instrumentId = instrumentId;
        this.quantity = quantity;
        this.executionPrice = executionPrice;
        this.direction = direction;
        this.clientId = clientId;
        this.order = order;
        this.tradeId = tradeId;
        this.cashValue = cashValue;
    }


    // Getters and Setters
    public String getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(String instrumentId) {
        this.instrumentId = instrumentId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getExecutionPrice() {
        return executionPrice;
    }

    public void setExecutionPrice(double executionPrice) {
        this.executionPrice = executionPrice;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public double getCashValue() {
        return cashValue;
    }

    public void setCashValue(double cashValue) {
        this.cashValue = cashValue;
    }

}
