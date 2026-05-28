package com.marshals.dto;

import java.util.List;

public class TradeHistoryResponse {

    private String clientId;
    private List<TradeResponse> trades;

    public TradeHistoryResponse() {}

    public TradeHistoryResponse(String clientId, List<TradeResponse> trades) {
        this.clientId = clientId;
        this.trades = trades;
    }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public List<TradeResponse> getTrades() { return trades; }
    public void setTrades(List<TradeResponse> trades) { this.trades = trades; }
}
