package com.fidelity.integration;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fidelity.models.ClientPortfolio;
import com.fidelity.models.Trade;

public class ClientTradeDaoImpl implements ClientTradeDao {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private DataSource dataSource;
	
	public ClientTradeDaoImpl(DataSource ds) {
		dataSource = ds;
	}

	@Override
	public ClientPortfolio getClientPortfolio(String clientId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addClientPortfolio(ClientPortfolio clientPortfolio) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateClientPortfolio(ClientPortfolio clientPortfolio) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getClientTradeHistory(String clientId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addTrade(Trade trade) {
		// TODO Auto-generated method stub
		
	}

}
