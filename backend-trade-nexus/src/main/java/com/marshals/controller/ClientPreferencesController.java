package com.marshals.controller;

import com.marshals.model.ClientPreferences;
import com.marshals.service.ClientPreferencesService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        assertOwnerOrAdmin(clientId);
        return ResponseEntity.ok(preferencesService.getByClientId(clientId));
    }

    @PostMapping
    public ResponseEntity<ClientPreferences> createPreferences(@RequestBody ClientPreferences preferences) {
        assertOwnerOrAdmin(preferences.getClientId());
        return ResponseEntity.ok(preferencesService.saveOrUpdate(preferences));
    }

    @PutMapping
    public ResponseEntity<ClientPreferences> updatePreferences(@RequestBody ClientPreferences preferences) {
        assertOwnerOrAdmin(preferences.getClientId());
        return ResponseEntity.ok(preferencesService.saveOrUpdate(preferences));
    }

    private void assertOwnerOrAdmin(String clientId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !auth.getPrincipal().equals(clientId)) {
            throw new AccessDeniedException("FORBIDDEN");
        }
    }
}
