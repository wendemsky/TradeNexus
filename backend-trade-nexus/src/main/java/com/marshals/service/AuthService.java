package com.marshals.service;

import com.marshals.dto.ClientProfile;
import com.marshals.dto.LoginRequest;
import com.marshals.dto.RegisterRequest;
import com.marshals.dto.TokenRefreshResponse;
import com.marshals.model.Client;
import com.marshals.model.ClientIdentification;
import com.marshals.repository.ClientIdentificationRepository;
import com.marshals.repository.ClientRepository;
import com.marshals.security.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

@Service
public class AuthService {

    private static final Set<String> INDIA_ID_TYPES = Set.of("Aadhar", "PAN");
    private static final Set<String> USA_ID_TYPES = Set.of("SSN");

    private final ClientRepository clientRepository;
    private final ClientIdentificationRepository identificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${app.initial-balance:10000}")
    private BigDecimal initialBalance;

    public AuthService(ClientRepository clientRepository,
                       ClientIdentificationRepository identificationRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.clientRepository = clientRepository;
        this.identificationRepository = identificationRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional(readOnly = true)
    public ClientProfile login(LoginRequest request) {
        Client client = clientRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NoSuchElementException("EMAIL_NOT_FOUND"));

        if (!passwordEncoder.matches(request.getPassword(), client.getPassword())) {
            throw new IllegalArgumentException("INVALID_CREDENTIALS");
        }

        String token = jwtUtil.issueToken(client.getClientId(), client.getEmail(), client.isAdmin());
        return new ClientProfile(client, token);
    }

    @Transactional
    public ClientProfile register(RegisterRequest request) {
        validateRegistrationRequest(request);

        if (clientRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("EMAIL_TAKEN");
        }

        checkIdUniqueness(request);

        String clientId = UUID.randomUUID().toString();
        Client client = new Client();
        client.setClientId(clientId);
        client.setEmail(request.getEmail());
        client.setPassword(passwordEncoder.encode(request.getPassword()));
        client.setName(request.getName());
        client.setDateOfBirth(parseDate(request.getDateOfBirth()));
        client.setCountry(request.getCountry());
        client.setAdmin(false);
        client.setCurrBalance(initialBalance.setScale(4));

        if (request.getIdentification() != null) {
            for (RegisterRequest.IdentificationEntry entry : request.getIdentification()) {
                ClientIdentification id = new ClientIdentification(clientId, entry.getType(), entry.getValue());
                id.setClient(client);
                client.getIdentification().add(id);
            }
        }

        clientRepository.save(client);

        String token = jwtUtil.issueToken(clientId, client.getEmail(), false);
        return new ClientProfile(client, token);
    }

    public TokenRefreshResponse refresh(String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new IllegalArgumentException("INVALID_TOKEN");
        }
        String token = bearerToken.substring(7);
        Claims claims = jwtUtil.validate(token);

        String newToken = jwtUtil.issueToken(
                claims.getSubject(),
                claims.get("email", String.class),
                Boolean.TRUE.equals(claims.get("isAdmin", Boolean.class))
        );
        String expiresAt = jwtUtil.expiryOf(newToken).toString();
        return new TokenRefreshResponse(newToken, expiresAt);
    }

    private void validateRegistrationRequest(RegisterRequest request) {
        if (request.getEmail() == null || request.getPassword() == null
                || request.getName() == null || request.getDateOfBirth() == null
                || request.getCountry() == null) {
            throw new IllegalArgumentException("MISSING_REQUIRED_FIELDS");
        }

        String pw = request.getPassword();
        if (pw.length() < 8
                || pw.equals(pw.toLowerCase())
                || pw.equals(pw.toUpperCase())
                || !pw.matches(".*\\d.*")) {
            throw new IllegalArgumentException("INVALID_PASSWORD_FORMAT");
        }

        if (!List.of("India", "USA").contains(request.getCountry())) {
            throw new IllegalArgumentException("INVALID_COUNTRY");
        }

        if (request.getIdentification() != null) {
            Set<String> allowed = "India".equals(request.getCountry()) ? INDIA_ID_TYPES : USA_ID_TYPES;
            for (RegisterRequest.IdentificationEntry entry : request.getIdentification()) {
                if (!allowed.contains(entry.getType())) {
                    throw new IllegalArgumentException("INVALID_ID_TYPE_FOR_COUNTRY");
                }
            }
        }
    }

    private void checkIdUniqueness(RegisterRequest request) {
        if (request.getIdentification() == null) return;
        for (RegisterRequest.IdentificationEntry entry : request.getIdentification()) {
            if (identificationRepository.countByTypeAndValue(entry.getType(), entry.getValue()) > 0) {
                throw new IllegalArgumentException("ID_TAKEN");
            }
        }
    }

    private LocalDate parseDate(String dateOfBirth) {
        try {
            return LocalDate.parse(dateOfBirth);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("INVALID_DATE_FORMAT — expected YYYY-MM-DD");
        }
    }
}
