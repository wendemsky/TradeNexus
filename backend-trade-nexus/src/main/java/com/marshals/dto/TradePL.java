package com.marshals.dto;

import java.math.BigDecimal;

public class TradePL {

    private String instrumentId;
    private String instrumentDescription;
    private String categoryId;
    private BigDecimal realizedPL;
    private BigDecimal unrealizedPL;
    private BigDecimal totalPL;

    public TradePL() {}

    public TradePL(String instrumentId, String instrumentDescription, String categoryId,
                   BigDecimal realizedPL, BigDecimal unrealizedPL) {
        this.instrumentId = instrumentId;
        this.instrumentDescription = instrumentDescription;
        this.categoryId = categoryId;
        this.realizedPL = realizedPL;
        this.unrealizedPL = unrealizedPL;
        this.totalPL = realizedPL.add(unrealizedPL);
    }

    public String getInstrumentId() { return instrumentId; }
    public void setInstrumentId(String instrumentId) { this.instrumentId = instrumentId; }

    public String getInstrumentDescription() { return instrumentDescription; }
    public void setInstrumentDescription(String instrumentDescription) { this.instrumentDescription = instrumentDescription; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public BigDecimal getRealizedPL() { return realizedPL; }
    public void setRealizedPL(BigDecimal realizedPL) { this.realizedPL = realizedPL; }

    public BigDecimal getUnrealizedPL() { return unrealizedPL; }
    public void setUnrealizedPL(BigDecimal unrealizedPL) { this.unrealizedPL = unrealizedPL; }

    public BigDecimal getTotalPL() { return totalPL; }
    public void setTotalPL(BigDecimal totalPL) { this.totalPL = totalPL; }
}
