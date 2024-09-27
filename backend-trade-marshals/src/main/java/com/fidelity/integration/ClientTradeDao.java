package com.fidelity.integration;

import java.math.BigDecimal;

import com.fidelity.models.ClientPortfolio;
import com.fidelity.models.Holding;
import com.fidelity.models.Trade;
import com.fidelity.models.TradeHistory;

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
