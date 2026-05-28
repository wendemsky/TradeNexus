package com.marshals.controller;

import com.marshals.dto.TradePL;
import com.marshals.dto.TradeHistoryResponse;
import com.marshals.model.Holding;
import com.marshals.service.ActivityReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/activity-report")
public class ActivityReportController {

    private final ActivityReportService activityReportService;

    public ActivityReportController(ActivityReportService activityReportService) {
        this.activityReportService = activityReportService;
    }

    @GetMapping("/holdings/{clientId}")
    public ResponseEntity<List<Holding>> getHoldings(@PathVariable String clientId) {
        assertOwnerOrAdmin(clientId);
        return ResponseEntity.ok(activityReportService.getHoldings(clientId));
    }

    @GetMapping("/trades/{clientId}")
    public ResponseEntity<TradeHistoryResponse> getTradeHistory(@PathVariable String clientId) {
        assertOwnerOrAdmin(clientId);
        return ResponseEntity.ok(activityReportService.getTradeHistory(clientId));
    }

    @GetMapping("/pl/{clientId}")
    public ResponseEntity<List<TradePL>> getPL(@PathVariable String clientId) {
        assertOwnerOrAdmin(clientId);
        return ResponseEntity.ok(activityReportService.getPLReport(clientId));
    }

    private void assertOwnerOrAdmin(String clientId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !auth.getPrincipal().equals(clientId)) {
            throw new AccessDeniedException("FORBIDDEN");
        }
    }
}
