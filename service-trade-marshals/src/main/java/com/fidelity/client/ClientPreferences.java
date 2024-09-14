package com.fidelity.client;

import java.util.Objects;

public class ClientPreferences {
	
	private String clientId;
	private String investmentPurpose;
	private String incomeCategory;
	private String lengthOfInvestment;
	private String percentageOfSpend;
	private int riskTolerance;
	private String acceptAdvisor;
	
	public ClientPreferences(String clientId, String investmentPurpose, String incomeCategory,
			String lengthOfInvestment, String percentageOfSpend, int riskTolerance, String acceptAdvisor) {
		this.clientId = clientId;
		this.investmentPurpose = investmentPurpose;
		this.incomeCategory = incomeCategory;
		this.lengthOfInvestment = lengthOfInvestment;
		this.percentageOfSpend = percentageOfSpend;
		this.riskTolerance = riskTolerance;
		this.acceptAdvisor = acceptAdvisor;
	}
	
	public String getClientId() {
		return this.clientId;
	}
	
	public void setIncomeCategory(String incomeCategory) {
		this.incomeCategory = incomeCategory;
	}
	
	public String getIncomeCategory() {
		return this.incomeCategory;
	}

	@Override
	public int hashCode() {
		return Objects.hash(acceptAdvisor, clientId, incomeCategory, investmentPurpose, lengthOfInvestment,
				percentageOfSpend, riskTolerance);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClientPreferences other = (ClientPreferences) obj;
		return Objects.equals(acceptAdvisor, other.acceptAdvisor) && Objects.equals(clientId, other.clientId)
				&& Objects.equals(incomeCategory, other.incomeCategory)
				&& Objects.equals(investmentPurpose, other.investmentPurpose)
				&& Objects.equals(lengthOfInvestment, other.lengthOfInvestment)
				&& Objects.equals(percentageOfSpend, other.percentageOfSpend) && riskTolerance == other.riskTolerance;
	}

	@Override
	public String toString() {
		return "ClientPreferences [clientId=" + clientId + ", investmentPurpose=" + investmentPurpose
				+ ", incomeCategory=" + incomeCategory + ", lengthOfInvestment=" + lengthOfInvestment
				+ ", percentageOfSpend=" + percentageOfSpend + ", riskTolerance=" + riskTolerance + ", acceptAdvisor="
				+ acceptAdvisor + "]";
	}
	
	
}
