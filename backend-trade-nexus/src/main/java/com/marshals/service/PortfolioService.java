package com.marshals.service;

import com.marshals.model.Client;
import com.marshals.model.Holding;
import com.marshals.model.HoldingId;
import com.marshals.repository.ClientRepository;
import com.marshals.repository.HoldingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class PortfolioService {

    private final HoldingRepository holdingRepository;
    private final ClientRepository clientRepository;

    public PortfolioService(HoldingRepository holdingRepository, ClientRepository clientRepository) {
        this.holdingRepository = holdingRepository;
        this.clientRepository = clientRepository;
    }

    // BUY — weighted average cost basis, balance deducted
    // cashValue = qty × askPrice × 1.001 (fee already included)
    @Transactional
    public void applyBuy(String clientId, String instrumentId, int qty, BigDecimal cashValue) {
        Optional<Holding> existing = holdingRepository
                .findByIdClientIdAndIdInstrumentId(clientId, instrumentId);

        if (existing.isPresent()) {
            Holding h = existing.get();
            // newAvgPrice = (oldAvgPrice × oldQty + cashValue) / (oldQty + qty)
            BigDecimal totalCost = h.getAvgPrice()
                    .multiply(BigDecimal.valueOf(h.getQuantity()))
                    .add(cashValue);
            int newQty = h.getQuantity() + qty;
            h.setAvgPrice(totalCost.divide(BigDecimal.valueOf(newQty), 4, RoundingMode.HALF_UP));
            h.setQuantity(newQty);
            holdingRepository.save(h);
        } else {
            Holding h = new Holding(clientId, instrumentId, qty,
                    cashValue.divide(BigDecimal.valueOf(qty), 4, RoundingMode.HALF_UP));
            holdingRepository.save(h);
        }

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new NoSuchElementException("CLIENT_NOT_FOUND"));
        client.setCurrBalance(client.getCurrBalance().subtract(cashValue));
        clientRepository.save(client);
    }

    // SELL — avgPrice unchanged, balance credited, holding removed at qty=0
    // cashValue = qty × bidPrice × 0.999 (fee already included)
    @Transactional
    public void applySell(String clientId, String instrumentId, int qty, BigDecimal cashValue) {
        Holding h = holdingRepository
                .findByIdClientIdAndIdInstrumentId(clientId, instrumentId)
                .orElseThrow(() -> new IllegalArgumentException("INSUFFICIENT_HOLDINGS"));

        int newQty = h.getQuantity() - qty;
        if (newQty == 0) {
            holdingRepository.delete(h);
        } else {
            h.setQuantity(newQty);
            holdingRepository.save(h);
        }

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new NoSuchElementException("CLIENT_NOT_FOUND"));
        client.setCurrBalance(client.getCurrBalance().add(cashValue));
        clientRepository.save(client);
    }
}
