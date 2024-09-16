package com.fidelity.trade;

import java.math.BigDecimal;

public class Price {
	private BigDecimal askPrice;
    private BigDecimal bidPrice;
    private String priceTimestamp;
    private Instrument instrument;
    
	public Price(BigDecimal askPrice, BigDecimal bidPrice, String priceTimestamp, Instrument instrument) {
		if (askPrice == null) {
	        throw new NullPointerException("askPrice cannot be null");
	    }
	    if (askPrice.compareTo(BigDecimal.ZERO) < 0) {
	        throw new IllegalArgumentException("askPrice cannot be negative");
	    }
	    if (bidPrice == null) {
	        throw new NullPointerException("bidPrice cannot be null");
	    }
	    if (bidPrice.compareTo(BigDecimal.ZERO) < 0) {
	        throw new IllegalArgumentException("bidPrice cannot be negative");
	    }
		this.askPrice = askPrice;
		this.bidPrice = bidPrice;
		this.priceTimestamp = priceTimestamp;
		this.instrument = instrument;
	}

	public BigDecimal getAskPrice() {
		return askPrice;
	}

	public BigDecimal getBidPrice() {
		return bidPrice;
	}

	public String getPriceTimestamp() {
		return priceTimestamp;
	}

	public Instrument getInstrument() {
		return instrument;
	}
}
