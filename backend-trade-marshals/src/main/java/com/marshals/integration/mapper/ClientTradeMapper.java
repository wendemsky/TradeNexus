package com.marshals.integration.mapper;

import java.math.BigDecimal;

import com.marshals.models.ClientPortfolio;
import com.marshals.models.Holding;

public interface ClientTradeMapper {

	//Client Portoflio
	ClientPortfolio getClientPortfolio(String clientId); 
	//Updation of Client Portfolio has 3 parts - UpdateClientBalanc, addHoldings and updateHoldings
	void updateClientBalance(String clientId, BigDecimal currBalance);
	void addClientHoldings(String clientId, Holding holdoing);
	void updateClientHoldings(String clientId, Holding holding); 
	

}
