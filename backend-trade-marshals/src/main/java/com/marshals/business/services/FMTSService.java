package com.marshals.business.services;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.marshals.business.FMTSValidatedClient;
import com.marshals.business.Order;
import com.marshals.business.Price;
import com.marshals.business.Trade;
import com.marshals.integration.FMTSDao;
import com.marshals.integration.FMTSException;
//Will call fmts dao methods
@Service("fmtsService")
public class FMTSService { 
	@Autowired
	private FMTSDao fmtsDao;
	/*Client Verification - Login and Registration*/
	//For Registration - No ClientId
	public FMTSValidatedClient verifyClient(String email) {
		try {
			if(email==null) throw new NullPointerException("Client Email cannot be null");
			FMTSValidatedClient validatedClient = fmtsDao.verifyClient(email);
			return validatedClient;
		} catch(NullPointerException e) {
			throw e;
		} catch(FMTSException e) { //Any Exception thrown from fmtsDao
			throw e;
		}
	}
	//For Login - With ClientId
	public FMTSValidatedClient verifyClient(String email, String clientId) { 
		try {
			if(email==null || clientId == null) throw new NullPointerException("Client Details cannot be null");
			FMTSValidatedClient validatedClient = fmtsDao.verifyClient(email,clientId);
			return validatedClient;
		} catch(NullPointerException e) {
			throw e;
		} catch(FMTSException e) { //Any Exception thrown from fmtsDao
			throw e;
		}
	}
 
	/*Functions for Trade Execution*/
	//To return list of live prices 
	public List<Price> getLivePrices() {
		try {
			List<Price> priceList = fmtsDao.getLivePrices();
			if(priceList == null || priceList.isEmpty())
				throw new FMTSException("No instrument live prices fetched from FMTS Service");
			return priceList;
		} catch(FMTSException e) { //Any Exception thrown from fmtsDao
			throw e;
		}
	}
	//Service that takes in order and returns trade
    public Trade createTrade(Order order) {
    	try {
    		if(order == null) {
        		throw new NullPointerException("Order cannot be null");
        	}
        	Trade trade = fmtsDao.createTrade(order);
        	if(trade==null) throw new FMTSException("Order is invalid, Cannot execute trade");
        	return trade;
    	} catch(NullPointerException e) {
    		throw e;
    	} catch(FMTSException e) { //Any Exception thrown from fmtsDao
			throw e;
		}		
    }
}