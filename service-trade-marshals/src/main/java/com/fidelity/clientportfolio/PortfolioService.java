package com.fidelity.clientportfolio;

public class PortfolioService {

	private ClientPortfolio portfolio;
	

    public void setMockPortfolio(ClientPortfolio portfolio) {
        this.portfolio = portfolio;
    }

    public ClientPortfolio getClientPortfolio(String clientId) {
    	 if (clientId == null) {
             throw new NullPointerException("Client ID must not be null");
         }
       
        return portfolio;
    }

    public ClientPortfolio updateClientPortfolio(ClientPortfolio clientPortfolio) {
       
    	 if (clientPortfolio == null) {
             throw new NullPointerException("Client portfolio must not be null");
         }
        return clientPortfolio;
    }
    
}
