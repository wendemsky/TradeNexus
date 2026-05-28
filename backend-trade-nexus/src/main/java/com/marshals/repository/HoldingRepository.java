package com.marshals.repository;

import com.marshals.model.Holding;
import com.marshals.model.HoldingId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface HoldingRepository extends JpaRepository<Holding, HoldingId> {
    List<Holding> findByIdClientId(String clientId);
    Optional<Holding> findByIdClientIdAndIdInstrumentId(String clientId, String instrumentId);
}
