package com.fidelity.integration;

import com.fidelity.models.ClientPortfolio;
import com.fidelity.models.Holding;
import com.fidelity.models.Trade;
import com.fidelity.models.TradeHistory;

public interface ClientTradeDao {
	
	//Client Portoflio
	ClientPortfolio getClientPortfolio(String clientId); 
	void addClientHoldings(String clientId, Holding holdoing); 
	void updateClientHoldings(String clientId, Holding holding); 
	
	
	//Trade history 
	TradeHistory getClientTradeHistory(String clientId); 
	void addTrade(Trade trade);

}
