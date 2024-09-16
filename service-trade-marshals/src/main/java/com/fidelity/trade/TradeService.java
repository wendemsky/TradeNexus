package com.fidelity.trade;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
//import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fidelity.clientportfolio.*;

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
}






