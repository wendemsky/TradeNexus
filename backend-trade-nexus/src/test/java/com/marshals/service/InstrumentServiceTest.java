package com.marshals.service;

import com.marshals.model.Instrument;
import com.marshals.repository.InstrumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstrumentServiceTest {

    @Mock InstrumentRepository instrumentRepository;
    @InjectMocks InstrumentService instrumentService;

    @Test
    void getAllInstruments_returnsRepositoryResult() {
        Instrument aapl = new Instrument();
        aapl.setInstrumentId("AAPL");
        aapl.setCategoryId("STOCK");

        Instrument us10y = new Instrument();
        us10y.setInstrumentId("US10Y");
        us10y.setCategoryId("GOVT");

        when(instrumentRepository.findAll()).thenReturn(List.of(aapl, us10y));

        List<Instrument> result = instrumentService.getAllInstruments();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Instrument::getInstrumentId)
                .containsExactly("AAPL", "US10Y");
        verify(instrumentRepository).findAll();
    }

    @Test
    void getAllInstruments_emptyRepository_returnsEmptyList() {
        when(instrumentRepository.findAll()).thenReturn(List.of());

        List<Instrument> result = instrumentService.getAllInstruments();

        assertThat(result).isEmpty();
    }
}
