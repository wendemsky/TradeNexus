package com.fidelity.services;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

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
import com.fidelity.integration.ClientTradeDao;
import com.fidelity.integration.ClientTradeDaoImpl;

public class TradeService {
	
	private static PortfolioService portfolioService = null;
	private static TradeHistoryService tradeHistoryService = null;
	private ClientTradeDao dao;
//	private static TradeHistoryService tradeHistoryService = null;
	
	private List<Price> priceList;
	
	public TradeService(ClientTradeDao dao) {
		priceList = FMTSService.getLivePrices(); //Get Live Prices from FMTSService
		//Initializing Portfolio and Trade History Service
		portfolioService = new PortfolioService(dao); // get and update client portfolio
		tradeHistoryService = new TradeHistoryService(dao); //Add trade history
		this.dao = dao;
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
        				//Updating portfolio and Trade history
        				portfolioService.updateClientPortfolio(trade);
        				tradeHistoryService.addTrade(trade);
            			return trade;
            		}
            	}
        	} else if (order.getDirection() == "S"){
        		//System.out.println(order.getClientId());
        		for(Holding holding: clientPortfolio.getHoldings()) {
        			//System.out.println(holding.getInstrumentId());
        			if(holding.getInstrumentId().equals(order.getInstrumentId())) {
        				//Call FMTS Service to create the trade
        				Trade trade = FMTSService.createTrade(order);
        				//Updating portfolio and Trade history
        				portfolioService.updateClientPortfolio(trade);
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
    
    public List<Price> recommendTopBuyInstruments(ClientPreferences preferences, BigDecimal currBalance){
    	try {
    		if(preferences.getAcceptAdvisor()==false) throw new UnsupportedOperationException("Cannot recommend with robo advisor without accepting to it");
    	    PriceScorer scorer = new PriceScorer(preferences);
            List<Price> recommendedPrice = new ArrayList<Price>();
            //System.out.println("Instruments before sorting -> " + priceList);
            for(Price trade: priceList) {
            	if(calculateScore(trade, preferences).compareTo(new BigDecimal(scorer.calculateScore()).divide(new BigDecimal(25))) < 0) {
            		if(currBalance.subtract(trade.getBidPrice()).compareTo(BigDecimal.ZERO)<0) //Buy Condition - Not enough balance
            			continue;
            		recommendedPrice.add(trade);
            	}
            }
//            Collections.sort(availableTrades, scorer);
            
            //System.out.println("Instruments after sorting -> " + recommendedPrice.toString());
            
            // Return top 5 trades or fewer if there aren't enough trades
            return priceList.size() > 5 ? recommendedPrice.subList(0, 5) : recommendedPrice;
    	} catch(UnsupportedOperationException e) {
    		throw e;
    	}
    }
    
    public List<Price> recommendTopSellInstruments(ClientPreferences preferences, List<Holding> userHoldings){
    	try {
    		if(preferences.getAcceptAdvisor()==false) throw new UnsupportedOperationException("Cannot recommend with robo advisor without accepting to it");
    		List<Holding> topSellTradesInHoldings = new ArrayList<>();
        	if(userHoldings.size() <= 5) {
//        		return everything
        		topSellTradesInHoldings = userHoldings; 
        	} else {
        		for (int i = 0; i < 5; i++) 
                {
                   // generating the index using Math.random()
                    int index = (int)(Math.random() * userHoldings.size());
                    topSellTradesInHoldings.add(userHoldings.get(index));  
                }
        	}
        	//Retrieve the list of prices corresponding to the holdings
        	List<Price> topSellTrades = new ArrayList<>();
        	for(Holding holding:topSellTradesInHoldings) {
        		for(Price trade:priceList) {
        			if(holding.getInstrumentId() == trade.getInstrument().getInstrumentId()) {
        				topSellTrades.add(trade);
        				break;
        			}
        				
        		}
        	}

//        	for(Price trade: topSellTrades) {
//        		System.out.println("Top instruments to sell -> " + trade.getInstrument().getInstrumentId() );
//        	}
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

        //System.out.println("Score for trade - " + trade.getInstrument().getInstrumentDescription() + " , Score -> " + score);
        return score;
    }

    
}






