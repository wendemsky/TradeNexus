package com.fidelity.clientportfolio;

import java.math.BigDecimal;
import java.util.Objects;

public class Holding {
     private String instrumentId;
     private int quantity;
     private BigDecimal avgPrice;

     // Constructors, getters, and setters

     public Holding( String instrumentId, int quantity, BigDecimal avgPrice) {
         this.instrumentId = instrumentId;
         this.quantity = quantity;
         this.avgPrice = avgPrice;
     }

     public String getInstrumentId() {
         return instrumentId;
     }

     public void setInstrumentId(String instrumentId) {
         this.instrumentId = instrumentId;
     }

     public int getQuantity() {
         return quantity;
     }
     
     public String getInstrumentDescription() {
    	 return this.instrumentDesc;
     }

     public void setQuantity(int quantity) {
         this.quantity = quantity;
     }

	public BigDecimal getAvgPrice() {
		return avgPrice;
	}

	public void setAvgPrice(BigDecimal avgPrice) {
		this.avgPrice = avgPrice;
	}

}
