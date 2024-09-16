package com.fidelity.trade;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
//import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fidelity.client.ClientPreferences;
import com.fidelity.clientportfolio.*;
import com.fidelity.roboadvisor.PriceScorer;

public class TradeService {

    private static final String FETCH_PRICES_API_URL = "http://localhost:3000/fmts/trades/prices";
//    private static final String EXECUTE_TRADE_API_URL = "http://localhost:3000/fmts/trades/trade";
    
    // Trade History
    private Map<String, List<Trade>> tradeHistory = new HashMap<>();

    // Get All Prices 
    public List<Price> getAllPrices() throws Exception {
        List<Price> prices = new ArrayList<>();

        URL url = new URL(FETCH_PRICES_API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONArray jsonArray = new JSONArray(response.toString());

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                BigDecimal askPrice = jsonObject.getBigDecimal("askPrice");
                BigDecimal bidPrice = jsonObject.getBigDecimal("bidPrice");
                String priceTimestamp = jsonObject.getString("priceTimestamp");

                JSONObject instrumentJson = jsonObject.getJSONObject("instrument");
                Instrument instrument = new Instrument(
                    instrumentJson.getString("instrumentId"),
                    instrumentJson.getString("externalIdType"),
                    instrumentJson.getString("externalId"),
                    instrumentJson.getString("categoryId"),
                    instrumentJson.getString("instrumentDescription"),
                    instrumentJson.getInt("maxQuantity"),
                    instrumentJson.getInt("minQuantity")
                );

                // Create Price object and add to list
                Price price = new Price(askPrice, bidPrice, priceTimestamp, instrument);
                prices.add(price);
            }
        } else {
            throw new RuntimeException("Failed to get prices. HTTP response code: " + responseCode);
        }

        return prices;
    }
    
    
//    public Trade executeTrade(Order order) throws Exception {
//        URL url = new URL(EXECUTE_TRADE_API_URL);
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setRequestMethod("POST");
//        connection.setRequestProperty("Content-Type", "application/json; utf-8");
//        connection.setRequestProperty("Accept", "application/json");
//        connection.setDoOutput(true);
//
//        JSONObject orderJson = new JSONObject();
//        orderJson.put("instrumentId", order.getInstrumentId());
//        orderJson.put("quantity", order.getQuantity());
//        orderJson.put("targetPrice", order.getTargetPrice());
//        orderJson.put("direction", order.getDirection());
//        orderJson.put("clientId", order.getClientId());
//        orderJson.put("orderId", order.getOrderId());
//        orderJson.put("token", order.getToken());
//
//        try (OutputStream os = connection.getOutputStream()) {
//            byte[] input = orderJson.toString().getBytes("utf-8");
//            os.write(input, 0, input.length);
//        }
//
//        int responseCode = connection.getResponseCode();
//        if (responseCode == HttpURLConnection.HTTP_OK) {
//            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//            String inputLine;
//            StringBuilder response = new StringBuilder();
//
//            while ((inputLine = in.readLine()) != null) {
//                response.append(inputLine);
//            }
//            in.close();
//
//            JSONObject responseJson = new JSONObject(response.toString());
//
////            JSONObject orderJsonResponse = responseJson.getJSONObject("order");
////            Order orderResponse = new Order(
////                orderJsonResponse.getString("instrumentId"),
////                orderJsonResponse.getInt("quantity"),
////                orderJsonResponse.getBigDecimal("targetPrice"),
////                orderJsonResponse.getString("direction"),
////                orderJsonResponse.getString("clientId"),
////                orderJsonResponse.getString("orderId"),
////                orderJsonResponse.getInt("token")
////            );
//
//            return new Trade(
//                responseJson.getString("instrumentId"),
//                responseJson.getInt("quantity"),
//                responseJson.getBigDecimal("executionPrice"),
//                responseJson.getString("direction"),
//                responseJson.getString("clientId"),
//                order,
//                responseJson.getString("tradeId"),
//                responseJson.getBigDecimal("cashValue")
//            );
//        } else {
//            throw new RuntimeException("Failed to execute trade. HTTP response code: " + responseCode);
//        }
//    }
   

    public Trade executeTrade(Order order) throws IllegalArgumentException {
        if (order == null) {
            throw new IllegalArgumentException("order cannot be null");
        }
        // Potentially add more validations for the order fields if needed

        Trade tradeResponse = new Trade(
                "N123456",
                10,
                new BigDecimal("104.25"),
                "S",
                "541107416",
                order,
                "aw6rqg2ee1q-pn1jh9yhg3s-ea6xxmv06bj",
                new BigDecimal("1052.925")
        );
        
//        addTradeToTradeHistory(tradeResponse);
        return tradeResponse;
    }

//    public void addTradeToTradeHistory(Trade trade) throws IllegalArgumentException {
//        if (trade == null) {
//            throw new IllegalArgumentException("trade cannot be null");
//        }
//        String clientId = trade.getClientId();
//        if (clientId == null || clientId.isEmpty()) {
//            throw new IllegalArgumentException("client ID cannot be null or empty");
//        }
//
//        List<Trade> trades = tradeHistory.getOrDefault(clientId, new ArrayList<>());
//        trades.add(trade);
//        tradeHistory.put(clientId, trades);
//    }
//
//    public List<Trade> getTrades(String clientId) throws IllegalArgumentException {
//        if (clientId == null || clientId.isEmpty()) {
//            throw new IllegalArgumentException("client ID cannot be null or empty");
//        }
//        return tradeHistory.getOrDefault(clientId, new ArrayList<>());
//    }
    
    
//    --------------------------------ROBO ADVISOR-------------------------------------------
    
    public List<Price> recommendTopTrades(List<Price> availableTrades, ClientPreferences preferences) {
        PriceScorer scorer = new PriceScorer(preferences);
        List<Price> recommendedPrice = new ArrayList<Price>();
        System.out.println("Trades before sorting -> " + availableTrades);
        for(Price trade: availableTrades) {
        	if(calculateScore(trade, preferences).compareTo(new BigDecimal(scorer.calculateScore()).divide(new BigDecimal(25))) < 0) {
        		recommendedPrice.add(trade);
        	}
        }
    
//        Collections.sort(availableTrades, scorer);
        
        System.out.println("Trades after sorting -> " + availableTrades.toString());
        
        // Return top 5 trades or fewer if there aren't enough trades
        return availableTrades.size() > 5 ? recommendedPrice.subList(0, 5) : recommendedPrice;
    }
    
    public List<Holding> recommendTopSellTrades(List<Holding> userHoldings){
    	
    	List<Holding> topSellTrades = new ArrayList<>();
    	if(userHoldings.size() <= 5) {
//    		return everything
    		topSellTrades = userHoldings; 
    	}else {
    		for (int i = 0; i < 5; i++) 
            {
               // generating the index using Math.random()
                int index = (int)(Math.random() * userHoldings.size());
                topSellTrades.add(userHoldings.get(index));
                
            }
    		
    	}
    	for(Holding userHolding: topSellTrades) {
    		System.out.println("Top sell trades -> " + userHolding.getInstrumentId() + " , Description -> " + userHolding.getInstrumentDescription());
    	}
    	
		return topSellTrades;
    	
    }
    
    
    public BigDecimal calculateScore(Price trade, ClientPreferences client) {
        BigDecimal bidAskSpread = (trade.getAskPrice().subtract(trade.getBidPrice())).setScale(4, RoundingMode.HALF_UP);
        BigDecimal price = trade.getAskPrice().setScale(4,RoundingMode.HALF_UP);
        BigDecimal score = BigDecimal.ZERO;
        BigDecimal value = new BigDecimal(1000);
        
        score = bidAskSpread.abs();
        
        if(bidAskSpread.abs().compareTo(new BigDecimal(1)) > 0) {
        	score = bidAskSpread.divide(new BigDecimal(1000));
        }

        // Calculate base score from bid-ask spread and price
//        score = value.multiply( bidAskSpread.abs()); // Simple scoring example
        
//        score = score.multiply(new BigDecimal(client.getRiskTolerance()).divide(new BigDecimal(5))); // Scale by risk tolerance
        // Adjust score based on client preferences

        System.out.println("Score for trade - " + trade.getInstrument().getInstrumentDescription() + " , Score -> " + score);
        return score;
    }
    
}






