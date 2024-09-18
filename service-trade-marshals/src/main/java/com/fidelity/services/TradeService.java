package com.fidelity.services;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.util.ArrayList;
import java.util.List;

//Importing models
import com.fidelity.models.ClientPortfolio;
import com.fidelity.models.ClientPreferences;
import com.fidelity.models.Holding;
import com.fidelity.models.Instrument;
import com.fidelity.models.Order;
import com.fidelity.models.Price;
import com.fidelity.models.Trade;
//Importing utils
import com.fidelity.utils.PriceScorer;
//Importing FMTS
import com.fidelity.fmts.FMTSService;

public class TradeService {
	
	private static PortfolioService portfolioService = null;
	private static TradeHistoryService tradeHistoryService = null;
	
	private List<Price> priceList;
	
	public TradeService() {
		priceList = new ArrayList<>();
		preparePriceList();
		//Initializing Portfolio and Trade History Service
		portfolioService = new PortfolioService();
		tradeHistoryService = new TradeHistoryService();
		List<Holding> holdings = new ArrayList<>();
		holdings.add(new Holding("N123456", 2, new BigDecimal("104.25")));
		List<ClientPortfolio> clientPortfolios = new ArrayList<ClientPortfolio>(
				List.of(
						new ClientPortfolio("1425922638", new BigDecimal("1000"), holdings),
						new ClientPortfolio("1425922634", new BigDecimal("2000"), holdings)
				)
			);
		portfolioService.addClientPortfolio(clientPortfolios.get(0));
		portfolioService.addClientPortfolio(clientPortfolios.get(1));
	}
   
	public void preparePriceList() {
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
	}
	
	public List<Price> getPriceList() {
		return priceList;
	}
	
    public Trade executeTrade(Order order) {
    	try {
    		if (order == null) {
                throw new NullPointerException("order cannot be null");
            }
        	
        	List<Price> prices = new ArrayList<>();
        
        	ClientPortfolio clientPortfolio = portfolioService.getClientPortfolio(order.getClientId());
        	
        	if(order.getDirection() == "B") {
        		for(Price price: priceList) {
            		if(price.getInstrument().getInstrumentId() == order.getInstrumentId()) {
            			//Call FMTS Service to create the trade
        				Trade trade = FMTSService.createTrade(order);
            			//Update portfolio
            			portfolioService.updateClientPortfolio(trade);
            			//Update trade history
            			tradeHistoryService.addTrade(trade);
            			return trade;
            		}
            	}
        	} else if (order.getDirection() == "S"){
        		for(Holding holding: clientPortfolio.getHoldings()) {
        			if(holding.getInstrumentId() == order.getInstrumentId()) {
        				//Call FMTS Service to create the trade
        				Trade trade = FMTSService.createTrade(order);
        				//Update portfolio
            			portfolioService.updateClientPortfolio(trade);
            			//Update trade history
            			tradeHistoryService.addTrade(trade);
            			return trade;
        			}
        		}
        	} else {
        		throw new IllegalArgumentException("Order direction is invalid");
        	}
            
            throw new IllegalArgumentException("Instrument is not present in the platform");
    	} catch(NullPointerException e) {
    		throw e;
    	} catch(IllegalArgumentException e) {
    		throw e;
    	}
    	
    }
    
     
    
//    --------------------------------ROBO ADVISOR-------------------------------------------
    
    public List<Price> recommendTopBuyInstruments(ClientPreferences preferences){
    	try {
    		if(preferences.getAcceptAdvisor()==false) throw new UnsupportedOperationException("Cannot recommend with robo advisor without accepting to it");
    	    PriceScorer scorer = new PriceScorer(preferences);
            List<Price> recommendedPrice = new ArrayList<Price>();
            System.out.println("Instruments before sorting -> " + priceList);
            for(Price trade: priceList) {
            	if(calculateScore(trade, preferences).compareTo(new BigDecimal(scorer.calculateScore()).divide(new BigDecimal(25))) < 0) {
            		recommendedPrice.add(trade);
            	}
            }
//            Collections.sort(availableTrades, scorer);
            
            System.out.println("Instruments after sorting -> " + recommendedPrice.toString());
            
            // Return top 5 trades or fewer if there aren't enough trades
            return priceList.size() > 5 ? recommendedPrice.subList(0, 5) : recommendedPrice;
    	} catch(UnsupportedOperationException e) {
    		throw e;
    	}
    }
    
    public List<Holding> recommendTopSellInstruments(ClientPreferences preferences, List<Holding> userHoldings){
    	try {
    		if(preferences.getAcceptAdvisor()==false) throw new UnsupportedOperationException("Cannot recommend with robo advisor without accepting to it");
    		List<Holding> topSellTrades = new ArrayList<>();
        	if(userHoldings.size() <= 5) {
//        		return everything
        		topSellTrades = userHoldings; 
        	} else {
        		for (int i = 0; i < 5; i++) 
                {
                   // generating the index using Math.random()
                    int index = (int)(Math.random() * userHoldings.size());
                    topSellTrades.add(userHoldings.get(index));  
                }
        	}
        	for(Holding userHolding: topSellTrades) {
        		System.out.println("Top instruments to sell -> " + userHolding.getInstrumentId() );
        	}
        	return topSellTrades;
    	} catch(UnsupportedOperationException e) {
    		throw e;
    	}	
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






