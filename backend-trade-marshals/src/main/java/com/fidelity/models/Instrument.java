package com.fidelity.models;

public class Instrument {
	private String instrumentId;
	private String externalIdType;
    private String externalId;
    private String categoryId;
    private String instrumentDescription;
    private Integer maxQuantity;
    private Integer minQuantity;
    
	public Instrument(String instrumentId, String externalIdType, String externalId, String categoryId,
			String instrumentDescription, Integer maxQuantity, Integer minQuantity) {
		if(instrumentId == null) {
			throw new NullPointerException("instrumentId cannot be null");
		}
		if(instrumentId.isEmpty()) {
			throw new IllegalArgumentException("instrumentId cannot be empty");
		}
		if(externalIdType == null) {
			throw new NullPointerException("externalIdType cannot be null");
		}
		if(externalIdType.isEmpty()) {
			throw new IllegalArgumentException("externalIdType cannot be empty");
		}
		if(externalId == null) {
			throw new NullPointerException("externalId cannot be null");
		}
		if(externalId.isEmpty()) {
			throw new IllegalArgumentException("externalId cannot be empty");
		}
		if(categoryId == null) {
			throw new NullPointerException("categoryId cannot be null");
		}
		if(categoryId.isEmpty()) {
			throw new IllegalArgumentException("categoryId cannot be empty");
		}
		if(instrumentDescription == null) {
			throw new NullPointerException("instrumentDescription cannot be null");
		}
		if(instrumentDescription.isEmpty()) {
			throw new IllegalArgumentException("instrumentDescription cannot be empty");
		}
		if(maxQuantity == null) {
			throw new NullPointerException("maxQuantity cannot be null");
		}
		if(maxQuantity < 0) {
			throw new IllegalArgumentException("maxQuantity cannot be negative");
		}
		if(minQuantity == null) {
			throw new NullPointerException("minQuantity cannot be null");
		}
		if(minQuantity < 0) {
			throw new IllegalArgumentException("minQuantity cannot be negative");
		}
		this.instrumentId = instrumentId;
		this.externalIdType = externalIdType;
		this.externalId = externalId;
		this.categoryId = categoryId;
		this.instrumentDescription = instrumentDescription;
		this.maxQuantity = maxQuantity;
		this.minQuantity = minQuantity;
	}

	public String getInstrumentId() {
		return instrumentId;
	}

	public String getExternalIdType() {
		return externalIdType;
	}

	public String getExternalId() {
		return externalId;
	}

	public String getCategoryId() {
		return categoryId;
	}

	public String getInstrumentDescription() {
		return instrumentDescription;
	}
	
	public Integer getMaxQuantity() {
		return maxQuantity;
	}

	public Integer getMinQuantity() {
		return minQuantity;
	}
    
    
}


