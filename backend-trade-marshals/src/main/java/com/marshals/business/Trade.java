package com.marshals.business;

import java.math.BigDecimal;
import java.util.Objects;

public class Trade {
    private String instrumentId;
    private Integer quantity;
    private BigDecimal executionPrice;
    private String direction;
    private String clientId;
    private Order order;
    private String tradeId;
    private BigDecimal cashValue;
    
    public Trade() {}

    public Trade(Order order, BigDecimal executionPrice, String tradeId, BigDecimal cashValue) {
        if (executionPrice == null || executionPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("executionPrice must be greater than 0");
        }
        if (tradeId == null || tradeId.isEmpty()) {
            throw new IllegalArgumentException("tradeId cannot be null or empty");
        }
        if (cashValue == null || cashValue.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("cashValue cannot be null and must be non-negative");
        }
        
        this.instrumentId = order.getInstrumentId();
        this.quantity = order.getQuantity();
        this.executionPrice = executionPrice;
        this.direction = order.getDirection();
        this.clientId = order.getClientId();
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

	@Override
	public int hashCode() {
		return Objects.hash(cashValue, clientId, direction, executionPrice, instrumentId, order, quantity, tradeId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Trade other = (Trade) obj;
		return Objects.equals(cashValue, other.cashValue) && Objects.equals(clientId, other.clientId)
				&& Objects.equals(direction, other.direction) && Objects.equals(executionPrice, other.executionPrice)
				&& Objects.equals(instrumentId, other.instrumentId) && Objects.equals(order, other.order)
				&& Objects.equals(quantity, other.quantity) && Objects.equals(tradeId, other.tradeId);
	}
    
    
}
