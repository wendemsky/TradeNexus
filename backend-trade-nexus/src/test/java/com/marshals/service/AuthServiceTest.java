package com.marshals.service;

import com.marshals.dto.ClientProfile;
import com.marshals.dto.LoginRequest;
import com.marshals.dto.RegisterRequest;
import com.marshals.model.Client;
import com.marshals.repository.ClientIdentificationRepository;
import com.marshals.repository.ClientRepository;
import com.marshals.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock ClientRepository clientRepository;
    @Mock ClientIdentificationRepository identificationRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtUtil jwtUtil;
    @InjectMocks AuthService authService;

    Client seededClient;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "initialBalance", new BigDecimal("10000"));
        seededClient = new Client();
        seededClient.setClientId("541107416");
        seededClient.setEmail("himanshu@gmail.com");
        seededClient.setPassword("hashed_password");
        seededClient.setName("Himanshu");
        seededClient.setDateOfBirth(LocalDate.of(2002, 12, 8));
        seededClient.setCountry("India");
        seededClient.setAdmin(false);
        seededClient.setCurrBalance(new BigDecimal("10000.0000"));
    }

    // ─── login ───────────────────────────────────────────────────────────────

    @Test
    void login_validCredentials_returnsProfileWithToken() {
        LoginRequest req = new LoginRequest();
        req.setEmail("himanshu@gmail.com");
        req.setPassword("Marsh2024");

        when(clientRepository.findByEmail("himanshu@gmail.com")).thenReturn(Optional.of(seededClient));
        when(passwordEncoder.matches("Marsh2024", "hashed_password")).thenReturn(true);
        when(jwtUtil.issueToken("541107416", "himanshu@gmail.com", false)).thenReturn("jwt-token");

        ClientProfile profile = authService.login(req);

        assertThat(profile.getToken()).isEqualTo("jwt-token");
        assertThat(profile.getClient().getClientId()).isEqualTo("541107416");
    }

    @Test
    void login_unknownEmail_throwsNoSuchElement() {
        LoginRequest req = new LoginRequest();
        req.setEmail("nobody@example.com");
        req.setPassword("Marsh2024");

        when(clientRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("EMAIL_NOT_FOUND");
    }

    @Test
    void login_wrongPassword_throwsIllegalArgument() {
        LoginRequest req = new LoginRequest();
        req.setEmail("himanshu@gmail.com");
        req.setPassword("WrongPass1");

        when(clientRepository.findByEmail("himanshu@gmail.com")).thenReturn(Optional.of(seededClient));
        when(passwordEncoder.matches("WrongPass1", "hashed_password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("INVALID_CREDENTIALS");
    }

    // ─── register ────────────────────────────────────────────────────────────

    @Test
    void register_validIndiaRequest_savesClientAndReturnsToken() {
        RegisterRequest req = validRequest("India");

        when(clientRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Marsh2024")).thenReturn("encoded");
        when(clientRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(jwtUtil.issueToken(anyString(), eq("new@example.com"), eq(false))).thenReturn("new-token");

        ClientProfile profile = authService.register(req);

        assertThat(profile.getToken()).isEqualTo("new-token");
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void register_validUsaRequest_savesClient() {
        RegisterRequest req = validRequest("USA");
        RegisterRequest.IdentificationEntry ssn = new RegisterRequest.IdentificationEntry();
        ssn.setType("SSN");
        ssn.setValue("123-45-6789");
        req.setIdentification(List.of(ssn));

        when(clientRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(identificationRepository.countByTypeAndValue("SSN", "123-45-6789")).thenReturn(0L);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(clientRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(jwtUtil.issueToken(anyString(), anyString(), anyBoolean())).thenReturn("token");

        ClientProfile profile = authService.register(req);

        assertThat(profile.getToken()).isEqualTo("token");
    }

    @Test
    void register_emailAlreadyTaken_throws() {
        RegisterRequest req = validRequest("India");
        when(clientRepository.existsByEmail("new@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("EMAIL_TAKEN");
    }

    @Test
    void register_nullPassword_throwsMissingFields() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("x@x.com");
        req.setName("Test");
        req.setDateOfBirth("1990-01-01");
        req.setCountry("India");

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("MISSING_REQUIRED_FIELDS");
    }

    @Test
    void register_passwordTooShort_throwsInvalidFormat() {
        RegisterRequest req = validRequest("India");
        req.setPassword("Ab1");

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("INVALID_PASSWORD_FORMAT");
    }

    @Test
    void register_passwordAllLowercase_throwsInvalidFormat() {
        RegisterRequest req = validRequest("India");
        req.setPassword("marsh2024");

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("INVALID_PASSWORD_FORMAT");
    }

    @Test
    void register_passwordNoDigit_throwsInvalidFormat() {
        RegisterRequest req = validRequest("India");
        req.setPassword("MarshPass");

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("INVALID_PASSWORD_FORMAT");
    }

    @Test
    void register_unsupportedCountry_throws() {
        RegisterRequest req = validRequest("Germany");

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("INVALID_COUNTRY");
    }

    @Test
    void register_ssnForIndiaClient_throwsInvalidIdType() {
        RegisterRequest req = validRequest("India");
        RegisterRequest.IdentificationEntry ssn = new RegisterRequest.IdentificationEntry();
        ssn.setType("SSN");
        ssn.setValue("123-45-6789");
        req.setIdentification(List.of(ssn));

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("INVALID_ID_TYPE_FOR_COUNTRY");
    }

    @Test
    void register_aadharForUsaClient_throwsInvalidIdType() {
        RegisterRequest req = validRequest("USA");
        RegisterRequest.IdentificationEntry aadhar = new RegisterRequest.IdentificationEntry();
        aadhar.setType("Aadhar");
        aadhar.setValue("123456789012");
        req.setIdentification(List.of(aadhar));

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("INVALID_ID_TYPE_FOR_COUNTRY");
    }

    @Test
    void register_duplicatePAN_throwsIdTaken() {
        RegisterRequest req = validRequest("India");
        RegisterRequest.IdentificationEntry pan = new RegisterRequest.IdentificationEntry();
        pan.setType("PAN");
        pan.setValue("ABCDE1234F");
        req.setIdentification(List.of(pan));

        when(clientRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(identificationRepository.countByTypeAndValue("PAN", "ABCDE1234F")).thenReturn(1L);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ID_TAKEN");
    }

    @Test
    void register_invalidDateFormat_throws() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("new@example.com");
        req.setPassword("Marsh2024");
        req.setName("Test");
        req.setDateOfBirth("08/12/2002");
        req.setCountry("India");

        when(clientRepository.existsByEmail("new@example.com")).thenReturn(false);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("INVALID_DATE_FORMAT");
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private RegisterRequest validRequest(String country) {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("new@example.com");
        req.setPassword("Marsh2024");
        req.setName("New User");
        req.setDateOfBirth("1990-01-01");
        req.setCountry(country);
        return req;
    }
}
