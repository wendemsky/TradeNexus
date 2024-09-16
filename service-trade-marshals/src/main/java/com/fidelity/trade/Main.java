//package com.fidelity.trade;
//
//import java.math.BigDecimal;
//import java.util.List;
//
//public class Main {
//
//    public static void main(String[] args) {
//        TradeService tradeService = new TradeService();
//        
//        //Try fetching price list
//        try {
//            List<Price> prices = tradeService.getAllPrices();
//
//            for (Price price : prices) {
//                System.out.println("Ask Price: " + price.getAskPrice());
//                System.out.println("Bid Price: " + price.getBidPrice());
//                System.out.println("Timestamp: " + price.getPriceTimestamp());
//                System.out.println("Instrument ID: " + price.getInstrument().getInstrumentId());
//                System.out.println("External ID Type: " + price.getInstrument().getExternalIdType());
//                System.out.println("External ID: " + price.getInstrument().getExternalId());
//                System.out.println("Category ID: " + price.getInstrument().getCategoryId());
//                System.out.println("Description: " + price.getInstrument().getInstrumentDescription());
//                System.out.println("Max Quantity: " + price.getInstrument().getMaxQuantity());
//                System.out.println("Min Quantity: " + price.getInstrument().getMinQuantity());
//                System.out.println("--------------------------------------------------");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
////        {
////        	"orderId": "PQR",
////        	"quantity": 10.0,
////        	"targetPrice": 104.5,
////        	"direction": "S",
////        	"clientId": "541107416",
////        	"instrumentId": "N123456",
////        	"token": 540983960
////        }
//        // Execute a mock trade
//        Order order = new Order(
//	        "N123456", 
//	        10, 
//	        new BigDecimal("104.5"), 
//	        "S", 
//	        "541107416", 
//	        "PQR", 
//	        540983960
//        );
//        
//        try {
//            // Execute trade
//            Trade trade = tradeService.executeTrade(order);
//            
//            System.out.println("Trade ID: " + trade.getTradeId());
//            System.out.println("Instrument ID: " + trade.getInstrumentId());
//            System.out.println("Quantity: " + trade.getQuantity());
//            System.out.println("Execution Price: " + trade.getExecutionPrice());
//            System.out.println("Direction: " + trade.getDirection());
//            System.out.println("Client ID: " + trade.getClientId());
//            System.out.println("Cash Value: " + trade.getCashValue());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        
//        
//        tradeService.executeTrade(order);
//        tradeService.executeTrade(order);
//        // Trade history should have 3 trades
//        System.out.println("-------------------------Trade History-------------------------");
//        List<Trade> trades = tradeService.getTrades("541107416");
//        for (Trade t : trades) {
//        	System.out.println("********** Trade **********\n");
//            System.out.println("Trade ID: " + t.getTradeId());
//            System.out.println("Instrument ID: " + t.getInstrumentId());
//            System.out.println("Quantity: " + t.getQuantity());
//            System.out.println("Execution Price: " + t.getExecutionPrice());
//            System.out.println("Direction: " + t.getDirection());
//            System.out.println("Client ID: " + t.getClientId());
//            System.out.println("Cash Value: " + t.getCashValue());
//        }
//    }
//}
//
//
//
