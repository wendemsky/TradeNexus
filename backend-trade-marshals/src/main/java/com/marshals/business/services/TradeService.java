package com.marshals.business.services;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.marshals.business.ClientPortfolio;
import com.marshals.business.ClientPreferences;
import com.marshals.business.FMTSValidatedClient;
import com.marshals.business.Holding;
import com.marshals.business.Order;
import com.marshals.business.Price;
import com.marshals.business.Trade;
import com.marshals.business.TradeHistory;
import com.marshals.integration.ClientTradeDao;
import com.marshals.utils.PriceScorer;

@Service("tradeService")
public class TradeService {
	
	private PortfolioService portfolioService;
	
	private ClientTradeDao dao; //Client Trade Dao that interacts with DB
	
	private List<Price> priceList;
	
	private FMTSService fmtsService;
	
	public TradeService(@Qualifier("clientTradeDao") ClientTradeDao dao, @Qualifier("portfolioService") PortfolioService portfolioService,  @Qualifier("fmtsService") FMTSService fmtsService) {
		//Initializing Portfolio Service
//		this.portfolioService = new PortfolioService(dao); // For getting and updating client portfolio
		this.dao = dao;
		this.portfolioService = portfolioService;
		this.fmtsService = fmtsService;
		priceList = fmtsService.getLivePrices(); //Get Live Prices from FMTSService
	}
   
	public List<Price> getPriceList() {
		return priceList;
	}
	
	//Adding the trade to the DB
	public void addTrade(Trade trade) {
		 if (trade == null) {
	         throw new NullPointerException("Trade must not be null");
		 }
	    dao.addTrade(trade);
	}
	
	//Executing the trade
    public Trade executeTrade(Order order) {
    	try {
    		if (order == null) {
                throw new NullPointerException("order cannot be null");
            }
        	
        	List<Price> prices = new ArrayList<>();
        
        	ClientPortfolio clientPortfolio = portfolioService.getClientPortfolio(order.getClientId());
        	
        	if(order.getDirection() == "B") {
        		for(Price price: priceList) {
            		if(price.getInstrument().getInstrumentId().equals(order.getInstrumentId())) {
            			//Call FMTS Service to create the trade
            			
        				Trade trade = fmtsService.createTrade(order);
        				trade.getOrder().setOrderId(order.getOrderId());
        				//Buy Condition Validation
        				//Getting the cost of trade and checking if its lesser than or equal to balance
        				BigDecimal totalCostOfTrade = trade.getCashValue();
    	                if (clientPortfolio.getCurrBalance().compareTo(totalCostOfTrade) >= 0) {	
	        				//Updating portfolio and adding trade
	        				portfolioService.updateClientPortfolio(trade);
	        				this.addTrade(trade);	
    	                } else {
    	                    throw new IllegalArgumentException("Insufficient balance! Cannot buy the instrument");
    	                }
    	                return trade;
            		}
            	}
        	} else if (order.getDirection() == "S"){
        		for(Holding holding: clientPortfolio.getHoldings()) {
        			//Sell condition checking
        			if(holding.getInstrumentId().equals(order.getInstrumentId())) {
        				//One more sell validation checking - To check if user has more quantity to sell
        				if(holding.getQuantity() >= order.getQuantity()) {
        					//Call FMTS Service to create the trade
            				Trade trade = fmtsService.createTrade(order);
            				trade.getOrder().setOrderId(order.getOrderId());
            				//Updating portfolio and adding Trade 
            				portfolioService.updateClientPortfolio(trade);
            				this.addTrade(trade);	
                			return trade;
        				} else {
        					throw new IllegalArgumentException("Insufficient quantity in holdings to sell the instrument");
        				}
        			}
        		}
        		//If above loop was completely executed, instrument wasnt part of holdings - SELL CONDITION VALIDATION
        		 throw new IllegalArgumentException("Instrument not part of holdings! Cannot sell the instrument");
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
    
     
    //Get Trade History
    public TradeHistory getClientTradeHistory(String clientId) {
        if (clientId == null) {
            throw new NullPointerException("Client ID must not be null");
        }
        TradeHistory tradeHistory;      
        tradeHistory = dao.getClientTradeHistory(clientId);
        return tradeHistory;
 
    }
    
//    --------------------------------ROBO ADVISOR-------------------------------------------
    
    public List<Price> recommendTopBuyInstruments(ClientPreferences preferences){
    	try {
    		BigDecimal currBalance = portfolioService.getClientPortfolio(preferences.getClientId()).getCurrBalance();
    		if(preferences.getAcceptAdvisor()=="false") throw new UnsupportedOperationException("Cannot recommend with robo advisor without accepting to it");
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
            
            //System.out.println("Instruments after sorting -> " + recommendedPrice.toString());
            
            // Return top 5 trades or fewer if there aren't enough trades
            return priceList.size() > 5 ? recommendedPrice.subList(0, 5) : recommendedPrice;
    	} catch(UnsupportedOperationException e) {
    		throw e;
    	}
    }
    
    public List<Price> recommendTopSellInstruments(ClientPreferences preferences){
    	try {
    		List<Holding> userHoldings = portfolioService.getClientPortfolio(preferences.getClientId()).getHoldings();
    		if(preferences.getAcceptAdvisor()=="false") throw new UnsupportedOperationException("Cannot recommend with robo advisor without accepting to it");
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






