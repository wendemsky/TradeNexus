package com.marshals.integration.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.marshals.models.Holding;

public interface ClientActivityReportMapper {
	List<Holding> getClientHoldings(@Param("clientId") String clientId);
}
