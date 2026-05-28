package com.marshals.controller;

import com.marshals.client.MdsClient;
import com.marshals.dto.OrderRequest;
import com.marshals.dto.Price;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Trade execution and robo advisor logic implemented in Session 2
@RestController
@RequestMapping("/trade")
public class TradeController {

    private final MdsClient mdsClient;

    public TradeController(MdsClient mdsClient) {
        this.mdsClient = mdsClient;
    }

    @GetMapping("/ping")
    public String ping() {
        return "Trade service is alive";
    }

    @GetMapping("/live-prices")
    public ResponseEntity<List<Price>> getLivePrices() {
        return ResponseEntity.ok(mdsClient.getAllPrices());
    }

    @GetMapping("/trade-history/{clientId}")
    public ResponseEntity<Void> getTradeHistory(@PathVariable String clientId) {
        return ResponseEntity.status(501).build();
    }

    @PostMapping("/execute-trade")
    public ResponseEntity<Void> executeTrade(@RequestBody OrderRequest order) {
        return ResponseEntity.status(501).build();
    }

    @PostMapping("/suggest-buy")
    public ResponseEntity<Void> suggestBuy(@RequestBody Object preferences) {
        return ResponseEntity.status(501).build();
    }

    @PostMapping("/suggest-sell")
    public ResponseEntity<Void> suggestSell(@RequestBody Object preferences) {
        return ResponseEntity.status(501).build();
    }
}
