package com.marshals.integration;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
 
import org.springframework.stereotype.Repository;
 
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
 
import com.marshals.business.FMTSValidatedClient;
import com.marshals.business.Order;
import com.marshals.business.Price;
import com.marshals.business.Trade;
 
@Repository("fmtsDao")
public class FMTSDaoImpl implements FMTSDao {
	private ObjectMapper objectMapper = new ObjectMapper();
 
	// FMTS endpoint
	final private String url = "http://localhost:3000/fmts";
 
	/* FOR CLIENT VALIDATION */
	// For Registration
	@Override
	public FMTSValidatedClient verifyClient(String email) {
		String api = "/client";
		try {
			HttpResponse<String> response = null;
			// Creating the JSON body sent in POST req
			ObjectNode jsonNode = objectMapper.createObjectNode();
			jsonNode.put("clientId", "");
			jsonNode.put("email", email);
			String jsonBody = objectMapper.writeValueAsString(jsonNode);
 
			//Sending request and creating client to receive response
			HttpRequest request = HttpRequest.newBuilder().uri(new URI(url + api))
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(jsonBody))
					.build();
			HttpClient client = HttpClient.newBuilder().build();
			response = client.send(request, HttpResponse.BodyHandlers.ofString());
			//Check for response status code
			if(response.statusCode()==406) { //Error in validation
				throw new FMTSException("FMTS couldnt validate new Client's Email");
			}
			else if(response.statusCode()!=200) { //Any other status code
				throw new FMTSException("There was an unexpected error from FMTS while validating new client");
			}
			//If successful response
			FMTSValidatedClient validatedClient = objectMapper.readValue(response.body(), FMTSValidatedClient.class);
			return validatedClient;
		} catch(FMTSException e) {
			throw e;
		}
		catch (Exception e) { //Any other unexpected error
			throw new FMTSException("There was an unexpected error from FMTS while validating new client");
		}
	}
 
	// For login
	@Override
	public FMTSValidatedClient verifyClient(String email, String clientId) {
		String api = "/client";
		try {
			HttpResponse<String> response = null;
			// Creating the JSON body sent in POST req
			ObjectNode jsonNode = objectMapper.createObjectNode();
			jsonNode.put("clientId", clientId);
			jsonNode.put("email", email);
			String jsonBody = objectMapper.writeValueAsString(jsonNode);
 
			//Sending request and creating client to receive response
			HttpRequest request = HttpRequest.newBuilder().uri(new URI(url + api))
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(jsonBody))
					.build();
			HttpClient client = HttpClient.newBuilder().build();
			response = client.send(request, HttpResponse.BodyHandlers.ofString());
			//Check for response status code
			if(response.statusCode()==406) { //Error in validation
				throw new FMTSException("Logging in Client's validation credentials dont match");
			}
			else if(response.statusCode()!=200) { //Any other status code
				throw new FMTSException("There was an unexpected error from FMTS while validating logging in client");
			}
			//If successful response
			FMTSValidatedClient validatedClient = objectMapper.readValue(response.body(), FMTSValidatedClient.class);
			return validatedClient;
		} catch(FMTSException e) {
			throw e;
		}
		catch (Exception e) { //Any other unexpected error
			throw new FMTSException("There was an unexpected error from FMTS while validating logging in client");
		}
	}
 
	/* FOR TRADE EXECUTION */

	// Get live prices
	@Override
	public List<Price> getLivePrices() {
		String api = "/trades/prices";
		HttpResponse<String> response = null;
		Price[] prices = null;
		try {
			HttpRequest request = 
					HttpRequest.newBuilder()
						.uri(new URI(url+api))
						.GET()
						.build();
			
			HttpClient client = HttpClient.newBuilder().build();
			response = client.send(request, HttpResponse.BodyHandlers.ofString());
			prices = objectMapper.readValue(response.body(), Price[].class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Arrays.asList(prices);	
	}

	// Execute trade
	@Override
	public Trade createTrade(Order order) {
		String api = "/trades/trade";
		HttpResponse<String> response = null;
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			String jsonBody = objectMapper.writeValueAsString(order);
			
			HttpRequest request = 
					HttpRequest.newBuilder()
						.uri(new URI(url+api))
						.POST(BodyPublishers.ofString(jsonBody))
						.header("Content-Type", "application/json")
						.build();
			
			HttpClient client = HttpClient.newBuilder().build();
			response = client.send(request, HttpResponse.BodyHandlers.ofString());
			System.out.println(response.body());
			Trade processedTrade = objectMapper.readValue(response.body(), Trade.class);
			System.out.println(processedTrade);
			return processedTrade;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new FMTSException("");
		}
		
	}
 
}
