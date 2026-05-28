package com.marshals.dto;

import com.marshals.model.ClientOrder;
import com.marshals.model.ClientTrade;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class TradeResponse {

    private String tradeId;
    private OrderDto order;
    private String instrumentId;
    private int quantity;
    private String direction;
    private String clientId;
    private BigDecimal executionPrice;
    private BigDecimal cashValue;
    private OffsetDateTime executedAt;

    public TradeResponse() {}

    public static TradeResponse from(ClientTrade trade) {
        ClientOrder o = trade.getOrder();
        TradeResponse r = new TradeResponse();
        r.tradeId = trade.getTradeId();
        r.order = OrderDto.from(o);
        r.instrumentId = o.getInstrument().getInstrumentId();
        r.quantity = o.getQuantity();
        r.direction = o.getDirection();
        r.clientId = o.getClient().getClientId();
        r.executionPrice = trade.getExecutionPrice();
        r.cashValue = trade.getCashValue();
        r.executedAt = trade.getExecutedAt();
        return r;
    }

    public String getTradeId() { return tradeId; }
    public OrderDto getOrder() { return order; }
    public String getInstrumentId() { return instrumentId; }
    public int getQuantity() { return quantity; }
    public String getDirection() { return direction; }
    public String getClientId() { return clientId; }
    public BigDecimal getExecutionPrice() { return executionPrice; }
    public BigDecimal getCashValue() { return cashValue; }
    public OffsetDateTime getExecutedAt() { return executedAt; }

    public static class OrderDto {
        private String orderId;
        private String instrumentId;
        private int quantity;
        private BigDecimal targetPrice;
        private String direction;
        private String orderType;
        private String clientId;

        public static OrderDto from(ClientOrder o) {
            OrderDto d = new OrderDto();
            d.orderId = o.getOrderId();
            d.instrumentId = o.getInstrument().getInstrumentId();
            d.quantity = o.getQuantity();
            d.targetPrice = o.getTargetPrice();
            d.direction = o.getDirection();
            d.orderType = o.getOrderType();
            d.clientId = o.getClient().getClientId();
            return d;
        }

        public String getOrderId() { return orderId; }
        public String getInstrumentId() { return instrumentId; }
        public int getQuantity() { return quantity; }
        public BigDecimal getTargetPrice() { return targetPrice; }
        public String getDirection() { return direction; }
        public String getOrderType() { return orderType; }
        public String getClientId() { return clientId; }
    }
}
