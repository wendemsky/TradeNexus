package com.fidelity.roboadvisor;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.fidelity.client.ClientPreferences;
import com.fidelity.trade.Instrument;
import com.fidelity.trade.Price;
import com.fidelity.trade.TradeService;

public class RoboAdvisor {
	
	private final static TradeService tradeService = new TradeService();

	 public static List<Price> recommendTopTrades(List<Price> availableTrades, ClientPreferences preferences) {
	        PriceScorer scorer = new PriceScorer(preferences);
	        List<Price> recommendedPrice = new ArrayList<Price>();
	        System.out.println("Trades before sorting -> " + availableTrades);
	        for(Price trade: availableTrades) {
	        	if(calculateScore(trade, preferences).compareTo(new BigDecimal(scorer.calculateScore()).divide(new BigDecimal(25))) < 0) {
	        		recommendedPrice.add(trade);
	        	}
	        }
	    
//	        Collections.sort(availableTrades, scorer);
	        
	        System.out.println("Trades after sorting -> " + availableTrades.toString());
	        
	        // Return top 5 trades or fewer if there aren't enough trades
	        return availableTrades.size() > 5 ? recommendedPrice.subList(0, 5) : recommendedPrice;
	    }

	    public static void main(String[] args) throws Exception {
	    	 
	        List<Price> trades = tradeService.getAllPrices();
	    	
	    	Instrument instrument1 = new Instrument("N123456", "CUSIP", "46625H100", "STOCK", "JPMorgan Chase & Co. Capital Stock", 1000, 1);
	    
//	    	List<Price> trades = Arrays.asList(
//	    			new Price(
//	    	    		new BigDecimal(104.75),
//	    	    		new BigDecimal(104.25),
//	    	    		"21-AUG-19 10.00.01.042000000 AM GMT",
//	    	    		instrument1
//	    	    		)
//	    			);

	        ClientPreferences prefs = new ClientPreferences(
	        "1",
	        "Retirement",
	        "VHIG",
	        "Long",
	        "Tier3",
	         2, 
	         "true"
	        );

	        List<Price> topBuys = recommendTopTrades(trades, prefs);
	        for (Price price : topBuys) {
	            System.out.println(price.getInstrument().getInstrumentId() + ": " + price.getAskPrice());
	        }
	    }
	    
	    public static BigDecimal calculateScore(Price trade, ClientPreferences client) {
	        BigDecimal bidAskSpread = (trade.getAskPrice().subtract(trade.getBidPrice())).setScale(4, RoundingMode.HALF_UP);
	        BigDecimal price = trade.getAskPrice().setScale(4,RoundingMode.HALF_UP);
	        BigDecimal score = BigDecimal.ZERO;
	        BigDecimal value = new BigDecimal(1000);
	        
	        score = bidAskSpread.abs();
	        
	        if(bidAskSpread.abs().compareTo(new BigDecimal(1)) > 0) {
	        	score = bidAskSpread.divide(new BigDecimal(1000));
	        }

	        // Calculate base score from bid-ask spread and price
//	        score = value.multiply( bidAskSpread.abs()); // Simple scoring example
	        
//	        score = score.multiply(new BigDecimal(client.getRiskTolerance()).divide(new BigDecimal(5))); // Scale by risk tolerance
	        // Adjust score based on client preferences

	        System.out.println("Score for trade - " + trade.getInstrument().getInstrumentDescription() + " , Score -> " + score);
	        return score;
	    }
}

