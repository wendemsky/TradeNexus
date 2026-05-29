package com.marshals.controller;

import com.marshals.model.ClientPreferences;
import com.marshals.security.SecurityUtils;
import com.marshals.service.ClientPreferencesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/client-preferences")
public class ClientPreferencesController {

    private final ClientPreferencesService preferencesService;

    public ClientPreferencesController(ClientPreferencesService preferencesService) {
        this.preferencesService = preferencesService;
    }

    @GetMapping("/ping")
    public String ping() {
        return "Preferences service is alive";
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<ClientPreferences> getPreferences(@PathVariable String clientId) {
        SecurityUtils.assertOwnerOrAdmin(clientId);
        return ResponseEntity.ok(preferencesService.getByClientId(clientId));
    }

    @PostMapping
    public ResponseEntity<ClientPreferences> createPreferences(@RequestBody ClientPreferences preferences) {
        SecurityUtils.assertOwnerOrAdmin(preferences.getClientId());
        return ResponseEntity.ok(preferencesService.saveOrUpdate(preferences));
    }

    @PutMapping
    public ResponseEntity<ClientPreferences> updatePreferences(@RequestBody ClientPreferences preferences) {
        SecurityUtils.assertOwnerOrAdmin(preferences.getClientId());
        return ResponseEntity.ok(preferencesService.saveOrUpdate(preferences));
    }
}
