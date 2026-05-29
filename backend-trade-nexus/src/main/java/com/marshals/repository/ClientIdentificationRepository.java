package com.marshals.repository;

import com.marshals.model.ClientIdentification;
import com.marshals.model.ClientIdentificationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClientIdentificationRepository extends JpaRepository<ClientIdentification, ClientIdentificationId> {

    @Query("SELECT COUNT(i) FROM ClientIdentification i WHERE i.id.type = :type AND i.value = :value")
    long countByTypeAndValue(@Param("type") String type, @Param("value") String value);
}
