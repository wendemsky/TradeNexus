package com.marshals.dto;

import java.math.BigDecimal;

public class Price {

    private String instrumentId;
    private String ticker;
    private BigDecimal lastPrice;
    private BigDecimal bidPrice;
    private BigDecimal askPrice;
    private String priceTimestamp;
    private boolean marketOpen;

    public Price() {}

    public String getInstrumentId() { return instrumentId; }
    public void setInstrumentId(String instrumentId) { this.instrumentId = instrumentId; }

    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }

    public BigDecimal getLastPrice() { return lastPrice; }
    public void setLastPrice(BigDecimal lastPrice) { this.lastPrice = lastPrice; }

    public BigDecimal getBidPrice() { return bidPrice; }
    public void setBidPrice(BigDecimal bidPrice) { this.bidPrice = bidPrice; }

    public BigDecimal getAskPrice() { return askPrice; }
    public void setAskPrice(BigDecimal askPrice) { this.askPrice = askPrice; }

    public String getPriceTimestamp() { return priceTimestamp; }
    public void setPriceTimestamp(String priceTimestamp) { this.priceTimestamp = priceTimestamp; }

    public boolean isMarketOpen() { return marketOpen; }
    public void setMarketOpen(boolean marketOpen) { this.marketOpen = marketOpen; }
}
