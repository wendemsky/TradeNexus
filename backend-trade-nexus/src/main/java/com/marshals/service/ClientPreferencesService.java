package com.marshals.service;

import com.marshals.model.ClientPreferences;
import com.marshals.repository.ClientPreferencesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class ClientPreferencesService {

    private final ClientPreferencesRepository preferencesRepository;

    public ClientPreferencesService(ClientPreferencesRepository preferencesRepository) {
        this.preferencesRepository = preferencesRepository;
    }

    @Transactional(readOnly = true)
    public ClientPreferences getByClientId(String clientId) {
        return preferencesRepository.findById(clientId)
                .orElseThrow(() -> new NoSuchElementException("PREFERENCES_NOT_FOUND"));
    }

    @Transactional
    public ClientPreferences saveOrUpdate(ClientPreferences preferences) {
        return preferencesRepository.save(preferences);
    }
}
