package com.marshals.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marshals.dto.LoginRequest;
import com.marshals.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the authentication flow.
 * Requires Docker PostgreSQL on port 5433 (docker-compose up in db-trade-nexus/).
 * Run with: ./mvnw verify
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "jwt.secret=integration_test_jwt_secret_must_be_at_least_32_chars",
                "spring.datasource.url=jdbc:postgresql://localhost:5433/tradenexus?TimeZone=UTC&stringtype=unspecified",
                "spring.datasource.username=tn_user",
                "spring.datasource.password=TradeNexus_local_2024",
                "spring.jpa.hibernate.ddl-auto=validate",
                "spring.flyway.enabled=false",
                "mds.url=http://localhost:3001"
        }
)
@AutoConfigureMockMvc
class AuthControllerIT {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;

    // ─── login ────────────────────────────────────────────────────────────────

    @Test
    void login_validSeededCredentials_returns200WithToken() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("himanshu@gmail.com");
        req.setPassword("Marsh2024");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.client.clientId").value("541107416"));
    }

    @Test
    void login_unknownEmail_returns404() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("nobody@example.com");
        req.setPassword("Marsh2024");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void login_wrongPassword_returns400() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("himanshu@gmail.com");
        req.setPassword("WrongPass1");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ─── register ─────────────────────────────────────────────────────────────

    @Test
    @Transactional
    void register_validRequest_returns200WithToken() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("integration_test_new@example.com");
        req.setPassword("Marsh2024");
        req.setName("Integration Test");
        req.setDateOfBirth("1990-06-15");
        req.setCountry("India");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.client.email").value("integration_test_new@example.com"));
    }

    @Test
    void register_duplicateEmail_returns400() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("himanshu@gmail.com"); // already in seed data
        req.setPassword("Marsh2024");
        req.setName("Duplicate");
        req.setDateOfBirth("1990-01-01");
        req.setCountry("India");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_invalidPassword_returns400() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("new_invalid@example.com");
        req.setPassword("weak");
        req.setName("Test");
        req.setDateOfBirth("1990-01-01");
        req.setCountry("India");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ─── security ─────────────────────────────────────────────────────────────

    @Test
    void protectedEndpoint_withoutToken_returns403() throws Exception {
        // Spring Security returns 403 for anonymous access (no AuthenticationEntryPoint configured)
        mockMvc.perform(get("/portfolio/client/541107416"))
                .andExpect(status().isForbidden());
    }

    @Test
    void protectedEndpoint_withValidToken_returns200() throws Exception {
        // First login to get a valid token
        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail("himanshu@gmail.com");
        loginReq.setPassword("Marsh2024");

        String responseBody = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(loginReq)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = mapper.readTree(responseBody).get("token").asText();

        mockMvc.perform(get("/portfolio/client/541107416")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpoint_withTokenForDifferentUser_returns403() throws Exception {
        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail("john.doe@gmail.com"); // client 739982664
        loginReq.setPassword("Marsh2024");

        String responseBody = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(loginReq)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = mapper.readTree(responseBody).get("token").asText();

        mockMvc.perform(get("/portfolio/client/541107416") // different client's data
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}
