package com.marshals.dto;

public class MarketStatus {

    private boolean marketOpen;
    private String timezone;
    private String currentTime;
    private String nextOpenAt;
    private String nextCloseAt;

    public MarketStatus() {}

    public boolean isMarketOpen() { return marketOpen; }
    public void setMarketOpen(boolean marketOpen) { this.marketOpen = marketOpen; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public String getCurrentTime() { return currentTime; }
    public void setCurrentTime(String currentTime) { this.currentTime = currentTime; }

    public String getNextOpenAt() { return nextOpenAt; }
    public void setNextOpenAt(String nextOpenAt) { this.nextOpenAt = nextOpenAt; }

    public String getNextCloseAt() { return nextCloseAt; }
    public void setNextCloseAt(String nextCloseAt) { this.nextCloseAt = nextCloseAt; }
}
