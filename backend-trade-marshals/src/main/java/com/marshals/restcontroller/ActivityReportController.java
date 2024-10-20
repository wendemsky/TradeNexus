package com.marshals.restcontroller;

import com.marshals.business.Holding;
import com.marshals.business.TradeHistory;
import com.marshals.business.services.ActivityReportService;
import com.marshals.integration.DatabaseException;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/activity-report")
public class ActivityReportController {
	
	@Autowired
	private Logger logger;
	
    private ActivityReportService activityReportService;

    public ActivityReportController(@Qualifier("activityReportService") ActivityReportService activityReportService) {
        this.activityReportService = activityReportService;
    }

    // Generate holdings report
    @GetMapping("/holdings/{clientId}")
    public ResponseEntity<List<Holding>> generateHoldingsReport(@PathVariable String clientId) {
        try {
            List<Holding> holdings = activityReportService.generateHoldingsReport(clientId);
            if(holdings == null || holdings.isEmpty()) {
            	return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
            return ResponseEntity.ok(holdings);
        } catch(NullPointerException e) {
			logger.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		} catch(DatabaseException e) {
			logger.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		} catch(RuntimeException e) {
			logger.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
    }

    // Generate trade report
    @GetMapping("/trades/{clientId}")
    public ResponseEntity<TradeHistory> generateTradeReport(@PathVariable String clientId) {
        try {
            TradeHistory tradeHistory = activityReportService.generateTradeReport(clientId);
            if(tradeHistory.getTrades() == null || tradeHistory.getTrades().isEmpty()) {
            	return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
            return ResponseEntity.ok(tradeHistory);
        } catch (NullPointerException e) {
        	logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch(DatabaseException e) {
			logger.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		} catch(RuntimeException e) {
			logger.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
    }

    // Generate P&L report
    @GetMapping("/pl/{clientId}")
    public ResponseEntity<Map<String, BigDecimal>> generatePLReport(@PathVariable String clientId) {
        try {
            Map<String, BigDecimal> profitLossMap = activityReportService.generatePLReport(clientId);
            if(profitLossMap == null || profitLossMap.isEmpty()) {
            	return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
            return ResponseEntity.ok(profitLossMap);
        } catch (NullPointerException e) {
        	logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch(DatabaseException e) {
			logger.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		} catch(RuntimeException e) {
			logger.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
    }
}
