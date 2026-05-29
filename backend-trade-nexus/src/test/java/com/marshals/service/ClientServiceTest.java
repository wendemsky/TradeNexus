package com.marshals.service;

import com.marshals.repository.ClientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock ClientRepository clientRepository;
    @InjectMocks ClientService clientService;

    @Test
    void emailExists_emailInDb_returnsTrue() {
        when(clientRepository.existsByEmail("himanshu@gmail.com")).thenReturn(true);

        assertThat(clientService.emailExists("himanshu@gmail.com")).isTrue();
    }

    @Test
    void emailExists_emailNotInDb_returnsFalse() {
        when(clientRepository.existsByEmail("nobody@example.com")).thenReturn(false);

        assertThat(clientService.emailExists("nobody@example.com")).isFalse();
    }
}
