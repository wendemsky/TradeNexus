package com.marshals.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Realized + unrealized P&L and full activity report implemented in Session 2
@RestController
@RequestMapping("/activity-report")
public class ActivityReportController {

    @GetMapping("/holdings/{clientId}")
    public ResponseEntity<Void> getHoldings(@PathVariable String clientId) {
        return ResponseEntity.status(501).build();
    }

    @GetMapping("/trades/{clientId}")
    public ResponseEntity<Void> getTradeHistory(@PathVariable String clientId) {
        return ResponseEntity.status(501).build();
    }

    @GetMapping("/pl/{clientId}")
    public ResponseEntity<Void> getPL(@PathVariable String clientId) {
        return ResponseEntity.status(501).build();
    }
}
