package com.marshals.integration;

import java.util.List;

import com.marshals.models.Holding;

public interface ClientActivityReportDao {
	
	//Holdings report
	List<Holding> getClientHoldings(String clientId);
	
	
	
}
