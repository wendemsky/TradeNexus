package com.fidelity.clientportfolio;

public class Order {
	 private String instrumentId;
	    private int quantity;
	    private double targetPrice;
	    private String direction;
	    private String clientId;
	    private String orderId;
	    private int token;

	    // Constructor
	    public Order(String instrumentId, int quantity, double targetPrice, String direction, String clientId,
	                 String orderId, int token) {
	        this.instrumentId = instrumentId;
	        this.quantity = quantity;
	        this.targetPrice = targetPrice;
	        this.direction = direction;
	        this.clientId = clientId;
	        this.orderId = orderId;
	        this.token = token;
	    }
}
