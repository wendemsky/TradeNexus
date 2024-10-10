package com.marshals.integration;

import java.math.BigDecimal;

import com.marshals.business.ClientPortfolio;
import com.marshals.business.Holding;

public interface ClientPortfolioDao {
	
	//Get Client Portoflio
	ClientPortfolio getClientPortfolio(String clientId); 
	//Updation of Client Portfolio has 3 parts - UpdateClientBalanc, addHoldings and updateHoldings
	void updateClientBalance(String clientId, BigDecimal currBalance);
	void addClientHoldings(String clientId, Holding holdoing);
	void updateClientHoldings(String clientId, Holding holding); 
}
