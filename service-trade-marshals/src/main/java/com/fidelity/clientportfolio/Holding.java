package com.fidelity.clientportfolio;

import java.math.BigDecimal;
import java.util.Objects;

public class Holding {
	 private String categoryId;
     private String instrumentId;
     private String instrumentDesc;
     private int quantity;
     private BigDecimal avgPrice;

     // Constructors, getters, and setters

     public Holding(String categoryId, String instrumentId, String instrumentDesc, int quantity, BigDecimal avgPrice) {
         this.categoryId = categoryId;
         this.instrumentId = instrumentId;
         this.instrumentDesc = instrumentDesc;
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

     public void setQuantity(int quantity) {
         this.quantity = quantity;
     }

	@Override
	public int hashCode() {
		return Objects.hash(avgPrice, categoryId, instrumentDesc, instrumentId, quantity);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Holding other = (Holding) obj;
		return Objects.equals(avgPrice, other.avgPrice) && Objects.equals(categoryId, other.categoryId)
				&& Objects.equals(instrumentDesc, other.instrumentDesc)
				&& Objects.equals(instrumentId, other.instrumentId) && quantity == other.quantity;
	}


}
