package com.marshals.dto;

import com.marshals.model.Holding;
import java.math.BigDecimal;
import java.util.List;

public class ClientPortfolioResponse {

    private String clientId;
    private BigDecimal currBalance;
    private List<Holding> holdings;

    public ClientPortfolioResponse() {}

    public ClientPortfolioResponse(String clientId, BigDecimal currBalance, List<Holding> holdings) {
        this.clientId = clientId;
        this.currBalance = currBalance;
        this.holdings = holdings;
    }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public BigDecimal getCurrBalance() { return currBalance; }
    public void setCurrBalance(BigDecimal currBalance) { this.currBalance = currBalance; }

    public List<Holding> getHoldings() { return holdings; }
    public void setHoldings(List<Holding> holdings) { this.holdings = holdings; }
}
