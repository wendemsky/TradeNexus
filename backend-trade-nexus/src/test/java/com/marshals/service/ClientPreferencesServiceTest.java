package com.marshals.service;

import com.marshals.model.ClientPreferences;
import com.marshals.repository.ClientPreferencesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientPreferencesServiceTest {

    @Mock ClientPreferencesRepository preferencesRepository;
    @InjectMocks ClientPreferencesService preferencesService;

    static final String CLIENT_ID = "541107416";

    ClientPreferences prefs;

    @BeforeEach
    void setUp() {
        prefs = new ClientPreferences();
        prefs.setClientId(CLIENT_ID);
        prefs.setInvestmentPurpose("Major Expense");
        prefs.setIncomeCategory("LIG");
        prefs.setLengthOfInvestment("Medium");
        prefs.setPercentageOfSpend("Tier2");
        prefs.setRiskTolerance((short) 1);
        prefs.setAcceptAdvisor(true);
    }

    // ─── getByClientId ────────────────────────────────────────────────────────

    @Test
    void getByClientId_existing_returnsPreferences() {
        when(preferencesRepository.findById(CLIENT_ID)).thenReturn(Optional.of(prefs));

        ClientPreferences result = preferencesService.getByClientId(CLIENT_ID);

        assertThat(result.getClientId()).isEqualTo(CLIENT_ID);
        assertThat(result.getRiskTolerance()).isEqualTo((short) 1);
        assertThat(result.isAcceptAdvisor()).isTrue();
    }

    @Test
    void getByClientId_notFound_throwsNoSuchElement() {
        when(preferencesRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> preferencesService.getByClientId("unknown"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("PREFERENCES_NOT_FOUND");
    }

    // ─── saveOrUpdate ─────────────────────────────────────────────────────────

    @Test
    void saveOrUpdate_newPreferences_savesAndReturns() {
        when(preferencesRepository.save(prefs)).thenReturn(prefs);

        ClientPreferences result = preferencesService.saveOrUpdate(prefs);

        assertThat(result).isSameAs(prefs);
        verify(preferencesRepository).save(prefs);
    }

    @Test
    void saveOrUpdate_updatedPreferences_savesWithNewValues() {
        prefs.setRiskTolerance((short) 4);
        prefs.setAcceptAdvisor(false);
        when(preferencesRepository.save(prefs)).thenReturn(prefs);

        ClientPreferences result = preferencesService.saveOrUpdate(prefs);

        assertThat(result.getRiskTolerance()).isEqualTo((short) 4);
        assertThat(result.isAcceptAdvisor()).isFalse();
        verify(preferencesRepository).save(prefs);
    }
}
