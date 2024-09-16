package com.fidelity.clientportfolio;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class PortfolioService {

	private ClientPortfolio portfolio;
	
	private List<ClientPortfolio> clientPortfolios;
	
	public PortfolioService() {
		this.clientPortfolios = new ArrayList<ClientPortfolio>();
	}
	

    public void setMockPortfolio(ClientPortfolio portfolio) {
        this.portfolio = portfolio;
    }
    
    public ClientPortfolio addClientPortfolio(ClientPortfolio clientPortfolio) {
     try {
	   	 if (clientPortfolio == null) {
	            throw new NullPointerException("Client portfolio must not be null");
	      }
	   	 clientPortfolios.add(clientPortfolio);
	   	 return clientPortfolio;
     } catch(NullPointerException e) {
    	 throw e;
     }
    
   }

    public ClientPortfolio getClientPortfolio(String clientId) {
    	try {
			if(clientId == null) {
				throw new NullPointerException("Id should not be null");
			}
			Iterator<ClientPortfolio> iter = clientPortfolios.iterator();
			while(iter.hasNext()) {
				ClientPortfolio portfolio = iter.next();
				if(portfolio.getClientId() == clientId) {
					return portfolio;
				}
			}
			throw new IllegalArgumentException("Client Portfolio is not existing");	
		} catch(NullPointerException e) {
			throw e;
		} catch(IllegalArgumentException e) {
			throw e;
		}
    }

    public ClientPortfolio updateClientPortfolio(ClientPortfolio clientPortfolio) {
       
    	 if (clientPortfolio == null) {
             throw new NullPointerException("Client portfolio must not be null");
         }
        return clientPortfolio;
    }
    
}
