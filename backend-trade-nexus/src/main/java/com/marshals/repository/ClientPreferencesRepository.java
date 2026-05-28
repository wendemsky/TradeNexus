package com.marshals.repository;

import com.marshals.model.ClientPreferences;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientPreferencesRepository extends JpaRepository<ClientPreferences, String> {
}
