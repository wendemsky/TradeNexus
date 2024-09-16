package com.fidelity.clientportfolio;
import java.util.List;
public class ClientPortfolio {
	 private String clientId;
	 private double currBalance;
	 private List<Holding> holdings;

	    // Constructors, getters, and setters

	    public ClientPortfolio(String clientId, double currBalance, List<Holding> holdings) {
	        this.clientId = clientId;
	        this.currBalance = currBalance;
	        this.holdings = holdings;
	    }

	    public String getClientId() {
	        return clientId;
	    }

	    public void setClientId(String clientId) {
	        this.clientId = clientId;
	    }

	    public double getCurrBalance() {
	        return currBalance;
	    }

	    public void setCurrBalance(double currBalance) {
	        this.currBalance = currBalance;
	    }

	    public List<Holding> getHoldings() {
	        return holdings;
	    }

	    public void setHoldings(List<Holding> holdings) {
	        this.holdings = holdings;
	    }
}
