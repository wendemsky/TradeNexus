package com.marshals.business.services;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.marshals.business.FIPSValidatedClient;
import com.marshals.business.Order;
import com.marshals.business.Price;
import com.marshals.business.Trade;
import com.marshals.integration.FIPSDao;
import com.marshals.integration.FIPSException;
//Will call fips dao methods
@Service("fipsService")
public class FIPSService { 
	@Autowired
	private FIPSDao fipsDao;
	/*Client Verification - Login and Registration*/
	//For Registration - No ClientId
	public FIPSValidatedClient verifyClient(String email) {
		try {
			if(email==null) throw new NullPointerException("Client Email cannot be null");
			FIPSValidatedClient validatedClient = fipsDao.verifyClient(email);
			return validatedClient;
		} catch(NullPointerException e) {
			throw e;
		} catch(FIPSException e) { //Any Exception thrown from fipsDao
			throw e;
		}
	}
	//For Login - With ClientId
	public FIPSValidatedClient verifyClient(String email, String clientId) { 
		try {
			if(email==null || clientId == null) throw new NullPointerException("Client Details cannot be null");
			FIPSValidatedClient validatedClient = fipsDao.verifyClient(email,clientId);
			return validatedClient;
		} catch(NullPointerException e) {
			throw e;
		} catch(FIPSException e) { //Any Exception thrown from fipsDao
			throw e;
		}
	}
 
	/*Functions for Trade Execution*/
	//To return list of live prices 
	public List<Price> getLivePrices() {
		try {
			List<Price> priceList = fipsDao.getLivePrices();
			return priceList;
		} catch(FIPSException e) { //Any Exception thrown from fipsDao
			throw e;
		}
	}
	//Service that takes in order and returns trade
    public Trade createTrade(Order order) {
    	try {
        	Trade trade = fipsDao.createTrade(order);
        	if(trade == null || trade.getTradeId() == null || trade.getTradeId().isBlank()) {
        		throw new FIPSException("Order is invalid, Cannot execute trade");
        	}
        	return trade;
    	} catch(FIPSException e) { //Any Exception thrown from fipsDao
			throw new FIPSException(e.getMessage());
    	} 	
    }
}