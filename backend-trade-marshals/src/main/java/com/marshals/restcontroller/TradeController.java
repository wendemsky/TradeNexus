package com.marshals.restcontroller;

import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.marshals.business.ClientPreferences;
import com.marshals.business.Order;
import com.marshals.business.Price;
import com.marshals.business.Trade;
import com.marshals.business.services.TradeService;
import com.marshals.integration.DatabaseException;
import com.marshals.integration.FMTSException;

@RestController
@RequestMapping("/trade")
public class TradeController {

	@Autowired
	private TradeService tradeService;

	@Autowired
	private Logger logger;
	// Get live instrument prices - from fmts - Directly call fmtsService
	// Display Trade History - call tradeService method
	// Execute trade - call tradeService executeTrade method
	// TradeService method itself takes care of updating portfolio, balance etc no
	// need to do here

	@PostMapping(value = "/execute-trade", 
			consumes = { MediaType.APPLICATION_JSON_VALUE }, 
			produces = { MediaType.APPLICATION_JSON_VALUE }
	)
	public ResponseEntity<Trade> executeTrade(@RequestBody Order order) {
		ResponseEntity<Trade> response;
		try {
			if (order == null) {
				 throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "order cannot be null");
			}
			Trade trade = tradeService.executeTrade(order);
			response = ResponseEntity.status(HttpStatus.OK).body(trade);
		} catch (ResponseStatusException e) {
			logger.error(e.getMessage());
			 throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getLocalizedMessage());
		} catch (NullPointerException e) {
			logger.error("Received null order", e);
			 throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getLocalizedMessage());
		} catch (IllegalArgumentException e) {
			logger.error("Illegal argument for trade execution", e);
			 throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getLocalizedMessage());
		} catch (DatabaseException e) {
			logger.error("Database error while executing trade", e);
			 throw new ResponseStatusException(HttpStatus.BAD_REQUEST,e.getLocalizedMessage());
		} catch(FMTSException e) {
			logger.error("FMTS Exception while executing trade", e);
			 throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getLocalizedMessage());
		} catch (RuntimeException e) {
			logger.error("An unexpected error occurred while executing trade", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		return response;
	}

	// Robo advisor - top 5 buy instruments : Must pass client preferences

	@PostMapping(value = "/suggest-buy", consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = {
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<List<Price>> getRoboAdvisorTopBuyInstruments(
			@RequestBody ClientPreferences clientPreferences) {
		ResponseEntity<List<Price>> response = null;
		try {
			if (clientPreferences == null) {
				throw new NullPointerException("Request body is null");
			}
			List<Price> recommendedBuyInstruments = tradeService.recommendTopBuyInstruments(clientPreferences);
			if (recommendedBuyInstruments == null) {
				throw new ResponseStatusException(HttpStatus.NO_CONTENT, "Cannot retrieve top buy instruments");
			}
			response = ResponseEntity.ok(recommendedBuyInstruments);
			return response;
		} catch (NullPointerException e) {
			throw new DatabaseException();
		} catch (ResponseStatusException e) {
			logger.error("Error in request for getting top buy trades from robo advisor", e);
			throw e;
		} catch (DatabaseException e) {
			logger.error("Error in fetching data", e);
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getLocalizedMessage());
		} catch (RuntimeException e) {
			logger.error("Problem occured from server", e);
			response = ResponseEntity.internalServerError().build();
			return response;
		}
	}

	// Robo advisor - top 5 sell instruments : Must pass client preferences
	@PostMapping("/suggest-sell")
	public ResponseEntity<List<Price>> getRoboAdvisorTopSellInstruments(
			@RequestBody ClientPreferences clienPreferences) {
		ResponseEntity<List<Price>> response = null;
		try {
			List<Price> recommendedSellInstruments = tradeService.recommendTopSellInstruments(clienPreferences);
			if (recommendedSellInstruments == null) {
				throw new ResponseStatusException(HttpStatus.NO_CONTENT, "Cannot retrieve top sell instruments");
			}
			response = ResponseEntity.ok(recommendedSellInstruments);
			return response;
		} catch (ResponseStatusException e) {
			logger.error("Error in request for getting top sell trades from robo advisor", e);
			throw e;
		} catch (DatabaseException e) {
			logger.error("Error in fetching data", e);
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getLocalizedMessage());
		} catch (RuntimeException e) {
			logger.error("Problem occured from server", e);
			response = ResponseEntity.internalServerError().build();
			return response;
		}
	}

}
