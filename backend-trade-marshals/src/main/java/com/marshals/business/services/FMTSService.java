package com.marshals.business.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.marshals.business.FMTSValidatedClient;
import com.marshals.business.Instrument;
import com.marshals.business.Order;
import com.marshals.business.Price;
import com.marshals.business.Trade;

//Has static methods - Mocking FMTS

@Service("fmtsService")
public class FMTSService {
	
	/*Client Verification - Login and Registration*/
	
	//For Registration - No ClientId
	public FMTSValidatedClient verifyClient(String email) {
		try {
			if(email==null) throw new NullPointerException("Client Email cannot be null");
			//Logic to generate client ID
			 int hashValue = 0;
			 for (char c : email.toCharArray()) {
	            hashValue = ((hashValue << 5) - hashValue) + (int) c; 
			 }
			 String clientId = String.valueOf(Math.abs(hashValue) + 123456);
			//Logic to generate token
			BigDecimal token = new BigDecimal(Math.abs(hashValue)).setScale(0);
//			System.out.println(clientId);
//			System.out.println(token);
			return new FMTSValidatedClient(email,clientId,token);
		} catch(NullPointerException e) {
			throw e;
		}
		
	}

	//For Login - With ClientId
	public FMTSValidatedClient verifyClient(String email, String clientId) { 
		try {
			if(email==null || clientId == null) throw new NullPointerException("Client Details cannot be null");
			//Logic to generate token
			int hashValue = 0;
			for (char c : email.toCharArray()) {
	           hashValue = ((hashValue << 5) - hashValue) + (int) c; 
			}
			BigDecimal token = new BigDecimal(Math.abs(hashValue)).setScale(0);
		
			return new FMTSValidatedClient(email,clientId,token);
		} catch(NullPointerException e) {
			throw e;
		}
	}
	
	
	/*Functions for Trade Execution*/
	
	//Static function that returns list of live prices 
	public List<Price> getLivePrices() {
		List<Price> priceList = new ArrayList<>();
		priceList.add(new Price(new BigDecimal("104.75"), new BigDecimal("104.25"), "21-AUG-19 10.00.01.042000000 AM GMT", 
		    new Instrument("N123456", "CUSIP", "46625H100", "STOCK", "JPMorgan Chase & Co. Capital Stock", 1000, 1)));

		priceList.add(new Price(new BigDecimal("312500"), new BigDecimal("312000"), "21-AUG-19 05.00.00.040000000 AM -05:00", 
		    new Instrument("N123789", "ISIN", "US0846707026", "STOCK", "Berkshire Hathaway Inc. Class A", 10, 1)));

		priceList.add(new Price(new BigDecimal("95.92"), new BigDecimal("95.42"), "21-AUG-19 10.00.02.042000000 AM GMT", 
		    new Instrument("C100", "CUSIP", "48123Y5A0", "CD", "JPMorgan Chase Bank, National Association 01/19", 1000, 100)));

		priceList.add(new Price(new BigDecimal("1.03375"), new BigDecimal("1.03390625"), "21-AUG-19 10.00.02.000000000 AM GMT", 
		    new Instrument("T67890", "CUSIP", "9128285M8", "GOVT", "USA, Note 3.125 15nov2028 10Y", 10000, 100)));

		priceList.add(new Price(new BigDecimal("0.998125"), new BigDecimal("0.99828125"), "21-AUG-19 10.00.02.002000000 AM GMT", 
		    new Instrument("T67894", "CUSIP", "9128285Z9", "GOVT", "USA, Note 2.5 31jan2024 5Y", 10000, 100)));

		priceList.add(new Price(new BigDecimal("1"), new BigDecimal("1.00015625"), "21-AUG-19 10.00.02.002000000 AM GMT", 
		    new Instrument("T67895", "CUSIP", "9128286A3", "GOVT", "USA, Note 2.625 31jan2026 7Y", 10000, 100)));

		priceList.add(new Price(new BigDecimal("0.999375"), new BigDecimal("0.999375"), "21-AUG-19 10.00.02.002000000 AM GMT", 
		    new Instrument("T67897", "CUSIP", "9128285X4", "GOVT", "USA, Note 2.5 31jan2021 2Y", 10000, 100)));

		priceList.add(new Price(new BigDecimal("0.999375"), new BigDecimal("0.999375"), "21-AUG-19 10.00.02.002000000 AM GMT", 
		    new Instrument("T67899", "CUSIP", "9128285V8", "GOVT", "USA, Notes 2.5% 15jan2022 3Y", 10000, 100)));

		priceList.add(new Price(new BigDecimal("1.00375"), new BigDecimal("1.00375"), "21-AUG-19 10.00.02.002000000 AM GMT", 
		    new Instrument("T67880", "CUSIP", "9128285U0", "GOVT", "USA, Note 1.5 31dec2023 5Y", 10000, 100)));

		priceList.add(new Price(new BigDecimal("1.0596875"), new BigDecimal("1.0596875"), "21-AUG-19 10.00.02.002000000 AM GMT", 
		    new Instrument("T67883", "CUSIP", "912810SE9", "GOVT", "USA, Bond 3.375 15nov2048 30Y", 10000, 100)));

		priceList.add(new Price(new BigDecimal("0.9853125"), new BigDecimal("0.98546875"), "21-AUG-19 10.00.02.002000000 AM GMT", 
		    new Instrument("T67878", "CUSIP", "912810SD1", "GOVT", "USA, Bond 3 15aug2048 30Y", 10000, 100)));

		priceList.add(new Price(new BigDecimal("1162.42"), new BigDecimal("1161.42"), "21-AUG-19 06.52.20.350000000 PM AMERICA/NEW_YORK", 
		    new Instrument("Q123", "CUSIP", "02079K107", "STOCK", "Alphabet Inc. Class C Capital Stock", 1000, 1)));

		priceList.add(new Price(new BigDecimal("323.39"), new BigDecimal("322.89"), "21-AUG-19 06.52.20.356000000 PM AMERICA/NEW_YORK", 
		    new Instrument("Q456", "CUSIP", "88160R101", "STOCK", "Tesla, Inc. Common Stock", 1000, 1)));
		return priceList;
	}
	
	//Static function that takes in order and returns trade
    public Trade createTrade(Order order) {
    	try {
    		if(order == null) {
        		throw new NullPointerException("Order cannot be null");
        	}
        	Trade trade = new Trade(
    			order, 
    			order.getTargetPrice(), //Executing at target price
    			"T"+order.getOrderId(), 
    			order.getTargetPrice().multiply(new BigDecimal(order.getQuantity())) //Cash value
        	);
    		return trade;
    	} catch(NullPointerException e) {
    		throw e;
    	}
    		
    }

}
