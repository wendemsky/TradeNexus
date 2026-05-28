package com.marshals.repository;

import com.marshals.model.ClientTrade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClientTradeRepository extends JpaRepository<ClientTrade, String> {
    List<ClientTrade> findByOrderClientClientId(String clientId);
}
