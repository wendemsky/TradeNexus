package com.marshals.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.math.BigDecimal;

@Entity
@Table(name = "holdings")
public class Holding {

    @EmbeddedId
    private HoldingId id;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "avg_price", precision = 19, scale = 4, nullable = false)
    private BigDecimal avgPrice;

    // Populated by PortfolioService from InstrumentRepository — not persisted
    @Transient
    private String instrumentDescription;

    @Transient
    private String categoryId;

    public Holding() {}

    public Holding(String clientId, String instrumentId, int quantity, BigDecimal avgPrice) {
        this.id = new HoldingId(clientId, instrumentId);
        this.quantity = quantity;
        this.avgPrice = avgPrice;
    }

    public HoldingId getId() { return id; }
    public void setId(HoldingId id) { this.id = id; }

    @JsonIgnore
    public String getClientId() { return id != null ? id.getClientId() : null; }

    public String getInstrumentId() { return id != null ? id.getInstrumentId() : null; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getAvgPrice() { return avgPrice; }
    public void setAvgPrice(BigDecimal avgPrice) { this.avgPrice = avgPrice; }

    public String getInstrumentDescription() { return instrumentDescription; }
    public void setInstrumentDescription(String instrumentDescription) { this.instrumentDescription = instrumentDescription; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
}
