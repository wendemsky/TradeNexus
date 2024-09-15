package com.fidelity.fmts;

import java.math.BigDecimal;

//Has static methods - Mocking FMTS

public class FMTSService {
	
	//Client Verification - Login and Registration
	
	//For Registration - No ClientId
	public static ValidatedClient verifyClient(String email) {
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
			return new ValidatedClient(email,clientId,token);
		} catch(NullPointerException e) {
			throw e;
		}
		
	}

	//For Login - With ClientId
	public static ValidatedClient verifyClient(String email, String clientId) { 
		try {
			if(email==null || clientId == null) throw new NullPointerException("Client Details cannot be null");
			//Logic to generate token
			int hashValue = 0;
			for (char c : email.toCharArray()) {
	           hashValue = ((hashValue << 5) - hashValue) + (int) c; 
			}
			BigDecimal token = new BigDecimal(Math.abs(hashValue)).setScale(0);
		
			return new ValidatedClient(email,clientId,token);
		} catch(NullPointerException e) {
			throw e;
		}
	}
	
	//Functions for Trade Execution
	//Static function that takes in order and returns trade

}
