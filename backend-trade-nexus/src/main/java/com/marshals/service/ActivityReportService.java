package com.marshals.service;

import com.marshals.client.MdsClient;
import com.marshals.dto.Price;
import com.marshals.dto.TradePL;
import com.marshals.dto.TradeHistoryResponse;
import com.marshals.dto.TradeResponse;
import com.marshals.model.ClientTrade;
import com.marshals.model.Holding;
import com.marshals.model.Instrument;
import com.marshals.repository.ClientTradeRepository;
import com.marshals.repository.HoldingRepository;
import com.marshals.repository.InstrumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ActivityReportService {

    private final HoldingRepository holdingRepository;
    private final ClientTradeRepository tradeRepository;
    private final InstrumentRepository instrumentRepository;
    private final MdsClient mdsClient;

    public ActivityReportService(HoldingRepository holdingRepository,
                                 ClientTradeRepository tradeRepository,
                                 InstrumentRepository instrumentRepository,
                                 MdsClient mdsClient) {
        this.holdingRepository = holdingRepository;
        this.tradeRepository = tradeRepository;
        this.instrumentRepository = instrumentRepository;
        this.mdsClient = mdsClient;
    }

    public List<Holding> getHoldings(String clientId) {
        List<Holding> holdings = holdingRepository.findByIdClientId(clientId);
        holdings.forEach(h -> instrumentRepository.findById(h.getInstrumentId()).ifPresent(i -> {
            h.setInstrumentDescription(i.getDescription());
            h.setCategoryId(i.getCategoryId());
        }));
        return holdings;
    }

    @Transactional(readOnly = true)
    public TradeHistoryResponse getTradeHistory(String clientId) {
        List<TradeResponse> trades = tradeRepository.findByOrderClientClientId(clientId)
                .stream()
                .sorted(Comparator.comparing(ClientTrade::getExecutedAt))
                .map(TradeResponse::from)
                .collect(Collectors.toList());
        return new TradeHistoryResponse(clientId, trades);
    }

    @Transactional(readOnly = true)
    public List<TradePL> getPLReport(String clientId) {
        List<ClientTrade> trades = tradeRepository.findByOrderClientClientId(clientId)
                .stream()
                .sorted(Comparator.comparing(ClientTrade::getExecutedAt))
                .collect(Collectors.toList());

        // Replay all trades to track running avgPrice per instrument — needed for realized P&L
        // realizedPL per sell = sellCashValue - (avgPrice × qtySold)   [BUSINESS_LOGIC.md §5.1]
        Map<String, BigDecimal> avgPriceMap   = new HashMap<>();
        Map<String, Integer>    qtyMap        = new HashMap<>();
        Map<String, BigDecimal> realizedPLMap = new HashMap<>();

        for (ClientTrade t : trades) {
            String instrumentId = t.getOrder().getInstrument().getInstrumentId();
            int qty = t.getOrder().getQuantity();
            BigDecimal cashValue = t.getCashValue();

            if ("B".equals(t.getOrder().getDirection())) {
                int oldQty = qtyMap.getOrDefault(instrumentId, 0);
                BigDecimal oldAvg = avgPriceMap.getOrDefault(instrumentId, BigDecimal.ZERO);
                int newQty = oldQty + qty;
                // Weighted average: (oldAvg × oldQty + cashValue) / newQty
                BigDecimal newAvg = oldAvg.multiply(BigDecimal.valueOf(oldQty))
                        .add(cashValue)
                        .divide(BigDecimal.valueOf(newQty), 4, RoundingMode.HALF_UP);
                avgPriceMap.put(instrumentId, newAvg);
                qtyMap.put(instrumentId, newQty);
            } else {
                // SELL: realizedPL = sellCashValue - (avgPrice × qtySold)
                BigDecimal avgPrice = avgPriceMap.getOrDefault(instrumentId, BigDecimal.ZERO);
                BigDecimal costBasis = avgPrice.multiply(BigDecimal.valueOf(qty));
                BigDecimal realizedPL = cashValue.subtract(costBasis).setScale(4, RoundingMode.HALF_UP);
                realizedPLMap.merge(instrumentId, realizedPL, BigDecimal::add);

                int remaining = qtyMap.getOrDefault(instrumentId, qty) - qty;
                if (remaining <= 0) {
                    qtyMap.remove(instrumentId);
                    avgPriceMap.remove(instrumentId);
                } else {
                    qtyMap.put(instrumentId, remaining);
                    // avgPrice unchanged on sell
                }
            }
        }

        // Unrealized P&L: (currentBidPrice - avgPrice) × currentQuantity   [BUSINESS_LOGIC.md §5.2]
        List<Holding> holdings = holdingRepository.findByIdClientId(clientId);
        Map<String, BigDecimal> liveBidPrices = mdsClient.getAllPrices().stream()
                .collect(Collectors.toMap(Price::getInstrumentId, Price::getBidPrice, (a, b) -> a));

        // Collect all instrument IDs that appear in either realized or unrealized
        Map<String, TradePL> plByInstrument = new HashMap<>();

        // Seed from realized trades
        for (Map.Entry<String, BigDecimal> e : realizedPLMap.entrySet()) {
            String id = e.getKey();
            Instrument instr = instrumentRepository.findById(id).orElse(null);
            plByInstrument.put(id, new TradePL(
                    id,
                    instr != null ? instr.getDescription() : id,
                    instr != null ? instr.getCategoryId() : "",
                    e.getValue(),
                    BigDecimal.ZERO
            ));
        }

        // Add unrealized from current holdings
        for (Holding h : holdings) {
            String id = h.getInstrumentId();
            BigDecimal bidPrice = liveBidPrices.get(id);
            if (bidPrice == null) continue;

            BigDecimal unrealized = bidPrice.subtract(h.getAvgPrice())
                    .multiply(BigDecimal.valueOf(h.getQuantity()))
                    .setScale(4, RoundingMode.HALF_UP);

            if (plByInstrument.containsKey(id)) {
                TradePL existing = plByInstrument.get(id);
                existing.setUnrealizedPL(unrealized);
                existing.setTotalPL(existing.getRealizedPL().add(unrealized));
            } else {
                Instrument instr = instrumentRepository.findById(id).orElse(null);
                TradePL entry = new TradePL(
                        id,
                        instr != null ? instr.getDescription() : id,
                        instr != null ? instr.getCategoryId() : "",
                        BigDecimal.ZERO,
                        unrealized
                );
                plByInstrument.put(id, entry);
            }
        }

        return new ArrayList<>(plByInstrument.values());
    }
}
