package com.fidelity.integration;

import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fidelity.models.Holding;

public class ClientActivityReportDaoImpl implements ClientActivityReportDao {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private DataSource dataSource;
	
	public ClientActivityReportDaoImpl (DataSource ds) {
		dataSource = ds;
	}

	@Override
	public List<Holding> getClientHoldings(String clientId) {
		// TODO Auto-generated method stub
		return null;
	}

}
