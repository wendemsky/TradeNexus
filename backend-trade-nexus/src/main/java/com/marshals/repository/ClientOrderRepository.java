package com.marshals.repository;

import com.marshals.model.ClientOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClientOrderRepository extends JpaRepository<ClientOrder, String> {
    List<ClientOrder> findByClientClientId(String clientId);
}
