package com.marshals.integration.mapper;
 
import java.util.List;
 
import org.apache.ibatis.annotations.Param;
import java.math.BigDecimal;
 
import com.marshals.models.ClientPortfolio;
import com.marshals.models.Holding;
import com.marshals.models.Order;
import com.marshals.models.Trade;

public interface ClientTradeMapper {
 
	//Client Portoflio
	ClientPortfolio getClientPortfolio(String clientId); 
	//Updation of Client Portfolio has 3 parts - UpdateClientBalanc, addHoldings and updateHoldings
	int updateClientBalance(@Param("clientId") String clientId,@Param("currBalance") BigDecimal currBalance);
	int addClientHoldings(@Param("clientId") String clientId, @Param("holding")Holding holding);
	int updateClientHoldings(@Param("clientId") String clientId, @Param("holding") Holding holding); 
	int addTrade(Trade trade);
	int addOrder(Order order);
	List<Trade> getClientTradeHistory(@Param("clientId") String clientId);
}
