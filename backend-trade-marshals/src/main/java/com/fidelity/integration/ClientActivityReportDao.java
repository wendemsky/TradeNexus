package com.fidelity.integration;

import java.util.List;

import com.fidelity.models.Holding;

public interface ClientActivityReportDao {
	
	//Holdings report
	List<Holding> getClientHoldings(String clientId);
	
	
	
}
