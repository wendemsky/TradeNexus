package com.marshals.controller;

import com.marshals.dto.ClientPortfolioResponse;
import com.marshals.model.Client;
import com.marshals.model.Holding;
import com.marshals.repository.ClientRepository;
import com.marshals.repository.HoldingRepository;
import com.marshals.repository.InstrumentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/portfolio")
public class PortfolioController {

    private final ClientRepository clientRepository;
    private final HoldingRepository holdingRepository;
    private final InstrumentRepository instrumentRepository;

    public PortfolioController(ClientRepository clientRepository,
                               HoldingRepository holdingRepository,
                               InstrumentRepository instrumentRepository) {
        this.clientRepository = clientRepository;
        this.holdingRepository = holdingRepository;
        this.instrumentRepository = instrumentRepository;
    }

    @GetMapping("/ping")
    public String ping() {
        return "Portfolio service is alive";
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<ClientPortfolioResponse> getPortfolio(@PathVariable String clientId) {
        assertOwnerOrAdmin(clientId);

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new NoSuchElementException("CLIENT_NOT_FOUND"));

        List<Holding> holdings = holdingRepository.findByIdClientId(clientId);
        enrichHoldings(holdings);

        return ResponseEntity.ok(new ClientPortfolioResponse(clientId, client.getCurrBalance(), holdings));
    }

    private void enrichHoldings(List<Holding> holdings) {
        for (Holding h : holdings) {
            instrumentRepository.findById(h.getInstrumentId()).ifPresent(instrument -> {
                h.setInstrumentDescription(instrument.getDescription());
                h.setCategoryId(instrument.getCategoryId());
            });
        }
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
