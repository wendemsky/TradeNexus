package com.marshals.integration;

import java.math.BigDecimal;

import com.marshals.models.ClientPortfolio;
import com.marshals.models.Holding;
import com.marshals.models.Trade;
import com.marshals.models.TradeHistory;

public interface ClientTradeDao {
	
	//Client Portoflio
	ClientPortfolio getClientPortfolio(String clientId); 
	//Updation of Client Portfolio has 3 parts - UpdateClientBalanc, addHoldings and updateHoldings
	void updateClientBalance(String clientId, BigDecimal currBalance);
	void addClientHoldings(String clientId, Holding holdoing);
	void updateClientHoldings(String clientId, Holding holding); 
	
	
	//Trade history 
	TradeHistory getClientTradeHistory(String clientId); //Change return type to TradeHistory once the model is created
	void addTrade(Trade trade);

}
