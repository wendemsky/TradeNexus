package com.marshals.controller;

import com.marshals.client.MdsClient;
import com.marshals.dto.OrderRequest;
import com.marshals.dto.Price;
import com.marshals.dto.TradeResponse;
import com.marshals.model.ClientPreferences;
import com.marshals.model.Holding;
import com.marshals.security.SecurityUtils;
import com.marshals.service.TradeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/trade")
public class TradeController {

    private final TradeService tradeService;
    private final MdsClient mdsClient;

    public TradeController(TradeService tradeService, MdsClient mdsClient) {
        this.tradeService = tradeService;
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

    @PostMapping("/execute-trade")
    public ResponseEntity<TradeResponse> executeTrade(@RequestBody OrderRequest order) {
        SecurityUtils.assertOwnerOrAdmin(order.getClientId());
        return ResponseEntity.ok(tradeService.executeTrade(order));
    }

    @PostMapping("/suggest-buy")
    public ResponseEntity<List<Price>> suggestBuy(@RequestBody ClientPreferences preferences) {
        SecurityUtils.assertOwnerOrAdmin(preferences.getClientId());
        return ResponseEntity.ok(tradeService.suggestBuy(preferences));
    }

    @PostMapping("/suggest-sell")
    public ResponseEntity<List<Holding>> suggestSell(@RequestBody ClientPreferences preferences) {
        SecurityUtils.assertOwnerOrAdmin(preferences.getClientId());
        return ResponseEntity.ok(tradeService.suggestSell(preferences));
    }
}
