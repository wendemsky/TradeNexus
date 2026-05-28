package com.marshals.service;

import com.marshals.model.ClientPreferences;
import com.marshals.repository.ClientPreferencesRepository;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class ClientPreferencesService {

    private final ClientPreferencesRepository preferencesRepository;

    public ClientPreferencesService(ClientPreferencesRepository preferencesRepository) {
        this.preferencesRepository = preferencesRepository;
    }

    public ClientPreferences getByClientId(String clientId) {
        return preferencesRepository.findById(clientId)
                .orElseThrow(() -> new NoSuchElementException("PREFERENCES_NOT_FOUND"));
    }

    public ClientPreferences saveOrUpdate(ClientPreferences preferences) {
        return preferencesRepository.save(preferences);
    }
}
