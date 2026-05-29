package com.marshals.controller;

import com.marshals.dto.ClientPortfolioResponse;
import com.marshals.security.SecurityUtils;
import com.marshals.service.PortfolioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping("/ping")
    public String ping() {
        return "Portfolio service is alive";
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<ClientPortfolioResponse> getPortfolio(@PathVariable String clientId) {
        SecurityUtils.assertOwnerOrAdmin(clientId);
        return ResponseEntity.ok(portfolioService.getPortfolio(clientId));
    }
}
