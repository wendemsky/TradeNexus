package com.fidelity.clientportfolio;

import java.math.BigDecimal;

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

     public String getCategoryId() {
         return categoryId;
     }

     public void setCategoryId(String categoryId) {
         this.categoryId = categoryId;
     }

     public String getInstrumentId() {
         return instrumentId;
     }

     public void setInstrumentId(String instrumentId) {
         this.instrumentId = instrumentId;
     }

     public String getInstrumentDesc() {
         return instrumentDesc;
     }

     public void setInstrumentDesc(String instrumentDesc) {
         this.instrumentDesc = instrumentDesc;
     }

     public int getQuantity() {
         return quantity;
     }

     public void setQuantity(int quantity) {
         this.quantity = quantity;
     }

}
