package com.marshals.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "client_preferences")
public class ClientPreferences {

    @Id
    @Column(name = "client_id")
    private String clientId;

    @Column(name = "investment_purpose", nullable = false)
    private String investmentPurpose;

    @Column(name = "income_category", nullable = false)
    private String incomeCategory;

    @Column(name = "length_of_investment", nullable = false)
    private String lengthOfInvestment;

    @Column(name = "percentage_of_spend", nullable = false)
    private String percentageOfSpend;

    @Column(name = "risk_tolerance", nullable = false)
    private int riskTolerance;

    @Column(name = "accept_advisor", nullable = false)
    private boolean acceptAdvisor;

    public ClientPreferences() {}

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getInvestmentPurpose() { return investmentPurpose; }
    public void setInvestmentPurpose(String investmentPurpose) { this.investmentPurpose = investmentPurpose; }

    public String getIncomeCategory() { return incomeCategory; }
    public void setIncomeCategory(String incomeCategory) { this.incomeCategory = incomeCategory; }

    public String getLengthOfInvestment() { return lengthOfInvestment; }
    public void setLengthOfInvestment(String lengthOfInvestment) { this.lengthOfInvestment = lengthOfInvestment; }

    public String getPercentageOfSpend() { return percentageOfSpend; }
    public void setPercentageOfSpend(String percentageOfSpend) { this.percentageOfSpend = percentageOfSpend; }

    public int getRiskTolerance() { return riskTolerance; }
    public void setRiskTolerance(int riskTolerance) { this.riskTolerance = riskTolerance; }

    public boolean isAcceptAdvisor() { return acceptAdvisor; }
    public void setAcceptAdvisor(boolean acceptAdvisor) { this.acceptAdvisor = acceptAdvisor; }
}
