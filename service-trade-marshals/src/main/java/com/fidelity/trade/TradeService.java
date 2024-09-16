package com.fidelity.trade;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fidelity.clientportfolio.Holding;

import java.io.BufferedReader;
import java.io.InputStreamReader;

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
	private PortfolioService portfolioService = new PortfolioService();

   
   
	public List<Price> getAllPrices() {
		List<Price> prices = new ArrayList<>();
		prices.add(new Price(new BigDecimal("104.75"), new BigDecimal("104.25"), "21-AUG-19 10.00.01.042000000 AM GMT", 
		    new Instrument("N123456", "CUSIP", "46625H100", "STOCK", "JPMorgan Chase & Co. Capital Stock", 1000, 1)));

		prices.add(new Price(new BigDecimal("312500"), new BigDecimal("312000"), "21-AUG-19 05.00.00.040000000 AM -05:00", 
		    new Instrument("N123789", "ISIN", "US0846707026", "STOCK", "Berkshire Hathaway Inc. Class A", 10, 1)));

		prices.add(new Price(new BigDecimal("95.92"), new BigDecimal("95.42"), "21-AUG-19 10.00.02.042000000 AM GMT", 
		    new Instrument("C100", "CUSIP", "48123Y5A0", "CD", "JPMorgan Chase Bank, National Association 01/19", 1000, 100)));

		prices.add(new Price(new BigDecimal("1.03375"), new BigDecimal("1.03390625"), "21-AUG-19 10.00.02.000000000 AM GMT", 
		    new Instrument("T67890", "CUSIP", "9128285M8", "GOVT", "USA, Note 3.125 15nov2028 10Y", 10000, 100)));

		prices.add(new Price(new BigDecimal("0.998125"), new BigDecimal("0.99828125"), "21-AUG-19 10.00.02.002000000 AM GMT", 
		    new Instrument("T67894", "CUSIP", "9128285Z9", "GOVT", "USA, Note 2.5 31jan2024 5Y", 10000, 100)));

		prices.add(new Price(new BigDecimal("1"), new BigDecimal("1.00015625"), "21-AUG-19 10.00.02.002000000 AM GMT", 
		    new Instrument("T67895", "CUSIP", "9128286A3", "GOVT", "USA, Note 2.625 31jan2026 7Y", 10000, 100)));

		prices.add(new Price(new BigDecimal("0.999375"), new BigDecimal("0.999375"), "21-AUG-19 10.00.02.002000000 AM GMT", 
		    new Instrument("T67897", "CUSIP", "9128285X4", "GOVT", "USA, Note 2.5 31jan2021 2Y", 10000, 100)));

		prices.add(new Price(new BigDecimal("0.999375"), new BigDecimal("0.999375"), "21-AUG-19 10.00.02.002000000 AM GMT", 
		    new Instrument("T67899", "CUSIP", "9128285V8", "GOVT", "USA, Notes 2.5% 15jan2022 3Y", 10000, 100)));

		prices.add(new Price(new BigDecimal("1.00375"), new BigDecimal("1.00375"), "21-AUG-19 10.00.02.002000000 AM GMT", 
		    new Instrument("T67880", "CUSIP", "9128285U0", "GOVT", "USA, Note 1.5 31dec2023 5Y", 10000, 100)));

		prices.add(new Price(new BigDecimal("1.0596875"), new BigDecimal("1.0596875"), "21-AUG-19 10.00.02.002000000 AM GMT", 
		    new Instrument("T67883", "CUSIP", "912810SE9", "GOVT", "USA, Bond 3.375 15nov2048 30Y", 10000, 100)));

		prices.add(new Price(new BigDecimal("0.9853125"), new BigDecimal("0.98546875"), "21-AUG-19 10.00.02.002000000 AM GMT", 
		    new Instrument("T67878", "CUSIP", "912810SD1", "GOVT", "USA, Bond 3 15aug2048 30Y", 10000, 100)));

		prices.add(new Price(new BigDecimal("1162.42"), new BigDecimal("1161.42"), "21-AUG-19 06.52.20.350000000 PM AMERICA/NEW_YORK", 
		    new Instrument("Q123", "CUSIP", "02079K107", "STOCK", "Alphabet Inc. Class C Capital Stock", 1000, 1)));

		prices.add(new Price(new BigDecimal("323.39"), new BigDecimal("322.89"), "21-AUG-19 06.52.20.356000000 PM AMERICA/NEW_YORK", 
		    new Instrument("Q456", "CUSIP", "88160R101", "STOCK", "Tesla, Inc. Common Stock", 1000, 1)));

		return prices;
	}
	
	
    public Trade executeTrade(Order order) throws IllegalArgumentException {
    	if (order == null) {
            throw new IllegalArgumentException("order cannot be null");
        }
    	
    	
    	List<Price> prices = new ArrayList<>();
    	List<Holding> holdings = new ArrayList<>();
    	PortfolioService portfolioService = new PortfolioService();
		ClientPortfolio clientPortfolio = portfolioService.getClientPortfolio(order.getClientId());
    	
    	try {
			 prices = getAllPrices();
			 holdings = clientPortfolio.getHoldings();	 
		} catch (Exception e) {
			e.printStackTrace();
		}
    	if(order.getDirection() == "B") {
    		for(Price price: prices) {
        		if(price.getInstrument().getInstrumentId() == order.getInstrumentId()) {
        			// updatePortfolio
        			return createTrade(order);
        		}
        	}
    	} else if (order.getDirection() == "S"){
    		for(Holding holding: holdings) {
    			if(holding.getInstrumentId() == order.getInstrumentId()) {
    				return createTrade(order);
    			}
    		}
    	} else {
    		throw new IllegalArgumentException("Order direction is invalid");
    	}
        
        throw new IllegalArgumentException("Instrument is not present in the platform");
    }
    
    public Trade createTrade(Order order) throws IllegalArgumentException {
    	Trade trade = new Trade(
			order.getInstrumentId(),
			order.getQuantity(), 
			order.getTargetPrice(), 
			order.getDirection(), 
			order.getClientId(), 
			order, 
			"id", 
			new BigDecimal("42")
    	);
		return trade;	
    }
    
    
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
    		System.out.println("Top sell trades -> " + userHolding.getInstrumentId() );
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






