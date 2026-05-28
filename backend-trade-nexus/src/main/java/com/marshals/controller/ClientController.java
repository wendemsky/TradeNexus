package com.marshals.controller;

import com.marshals.dto.IsVerifiedClient;
import com.marshals.repository.ClientRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/client")
public class ClientController {

    private final ClientRepository clientRepository;

    public ClientController(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @GetMapping("/ping")
    public String ping() {
        return "Client service is alive";
    }

    @GetMapping("/verify-email/{email}")
    public ResponseEntity<IsVerifiedClient> verifyEmail(@PathVariable String email) {
        boolean exists = clientRepository.existsByEmail(email);
        return ResponseEntity.ok(new IsVerifiedClient(exists));
    }
}
