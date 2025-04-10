package com.crewmeister.currencybackend.service.impl;

import com.crewmeister.currencybackend.dto.CurrencyDto;
import com.crewmeister.currencybackend.entity.Currency;
import com.crewmeister.currencybackend.exception.CurrencyNotFoundException;
import com.crewmeister.currencybackend.mapper.CurrencyMapper;
import com.crewmeister.currencybackend.repository.CurrencyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CurrencyServiceImplTest {

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private CurrencyMapper currencyMapper;

    @InjectMocks
    private CurrencyServiceImpl currencyService;

    private Currency usdCurrency;
    private Currency eurCurrency;
    private Currency gbpCurrency;
    private Currency inactiveJpyCurrency;
    private CurrencyDto usdCurrencyDto;
    private CurrencyDto eurCurrencyDto;
    private CurrencyDto gbpCurrencyDto;
    private CurrencyDto inactiveJpyCurrencyDto;
    private List<Currency> allCurrencies;
    private List<Currency> activeCurrencies;
    private List<Currency> usCurrencies;

    @BeforeEach
    void setUp() {
        // Setup entity test data
        usdCurrency = new Currency();
        usdCurrency.setCode("USD");
        usdCurrency.setName("US Dollar");
        usdCurrency.setCountry("United States");
        usdCurrency.setActive(true);

        eurCurrency = new Currency();
        eurCurrency.setCode("EUR");
        eurCurrency.setName("Euro");
        eurCurrency.setCountry("European Union");
        eurCurrency.setActive(true);

        gbpCurrency = new Currency();
        gbpCurrency.setCode("GBP");
        gbpCurrency.setName("British Pound");
        gbpCurrency.setCountry("United Kingdom");
        gbpCurrency.setActive(true);

        inactiveJpyCurrency = new Currency();
        inactiveJpyCurrency.setCode("JPY");
        inactiveJpyCurrency.setName("Japanese Yen");
        inactiveJpyCurrency.setCountry("Japan");
        inactiveJpyCurrency.setActive(false);

        // Setup DTO test data
        usdCurrencyDto = new CurrencyDto();
        usdCurrencyDto.setCode("USD");
        usdCurrencyDto.setName("US Dollar");
        usdCurrencyDto.setCountry("United States");
        usdCurrencyDto.setActive(true);

        eurCurrencyDto = new CurrencyDto();
        eurCurrencyDto.setCode("EUR");
        eurCurrencyDto.setName("Euro");
        eurCurrencyDto.setCountry("European Union");
        eurCurrencyDto.setActive(true);

        gbpCurrencyDto = new CurrencyDto();
        gbpCurrencyDto.setCode("GBP");
        gbpCurrencyDto.setName("British Pound");
        gbpCurrencyDto.setCountry("United Kingdom");
        gbpCurrencyDto.setActive(true);

        inactiveJpyCurrencyDto = new CurrencyDto();
        inactiveJpyCurrencyDto.setCode("JPY");
        inactiveJpyCurrencyDto.setName("Japanese Yen");
        inactiveJpyCurrencyDto.setCountry("Japan");
        inactiveJpyCurrencyDto.setActive(false);

        // Setup lists
        allCurrencies = Arrays.asList(usdCurrency, eurCurrency, gbpCurrency, inactiveJpyCurrency);
        activeCurrencies = Arrays.asList(usdCurrency, eurCurrency, gbpCurrency);
        usCurrencies = Collections.singletonList(usdCurrency);

        // NOT: BeforeEach içinde mapper stub'larını kaldırdık
        // Bu stub'ları her test metodunun içinde gerektiği yerde tanımlayacağız
    }

    @Test
    @DisplayName("Should return all currencies")
    void shouldReturnAllCurrencies() {
        // Given
        given(currencyRepository.findAll()).willReturn(allCurrencies);
        given(currencyMapper.mapToDto(usdCurrency)).willReturn(usdCurrencyDto);
        given(currencyMapper.mapToDto(eurCurrency)).willReturn(eurCurrencyDto);
        given(currencyMapper.mapToDto(gbpCurrency)).willReturn(gbpCurrencyDto);
        given(currencyMapper.mapToDto(inactiveJpyCurrency)).willReturn(inactiveJpyCurrencyDto);

        // When
        List<CurrencyDto> result = currencyService.getAllCurrencies();

        // Then
        assertThat(result).hasSize(4);
        assertThat(result).containsExactly(usdCurrencyDto, eurCurrencyDto, gbpCurrencyDto, inactiveJpyCurrencyDto);
        verify(currencyRepository).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no currencies found")
    void shouldReturnEmptyListWhenNoCurrenciesFound() {
        // Given
        given(currencyRepository.findAll()).willReturn(Collections.emptyList());

        // When
        List<CurrencyDto> result = currencyService.getAllCurrencies();

        // Then
        assertThat(result).isEmpty();
        verify(currencyRepository).findAll();
    }

    @Test
    @DisplayName("Should return only active currencies")
    void shouldReturnActiveCurrencies() {
        // Given
        given(currencyRepository.findByActiveTrue()).willReturn(activeCurrencies);
        given(currencyMapper.mapToDto(usdCurrency)).willReturn(usdCurrencyDto);
        given(currencyMapper.mapToDto(eurCurrency)).willReturn(eurCurrencyDto);
        given(currencyMapper.mapToDto(gbpCurrency)).willReturn(gbpCurrencyDto);

        // When
        List<CurrencyDto> result = currencyService.getActiveCurrencies();

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).containsExactly(usdCurrencyDto, eurCurrencyDto, gbpCurrencyDto);
        assertThat(result).doesNotContain(inactiveJpyCurrencyDto);
        verify(currencyRepository).findByActiveTrue();
    }

    @Test
    @DisplayName("Should return empty list when no active currencies found")
    void shouldReturnEmptyListWhenNoActiveCurrenciesFound() {
        // Given
        given(currencyRepository.findByActiveTrue()).willReturn(Collections.emptyList());

        // When
        List<CurrencyDto> result = currencyService.getActiveCurrencies();

        // Then
        assertThat(result).isEmpty();
        verify(currencyRepository).findByActiveTrue();
    }

    @Test
    @DisplayName("Should return currency by code")
    void shouldReturnCurrencyByCode() {
        // Given
        String currencyCode = "USD";
        given(currencyRepository.findByCode(currencyCode)).willReturn(Optional.of(usdCurrency));
        given(currencyMapper.mapToDto(usdCurrency)).willReturn(usdCurrencyDto);

        // When
        CurrencyDto result = currencyService.getCurrencyByCode(currencyCode);

        // Then
        assertThat(result).isEqualTo(usdCurrencyDto);
        verify(currencyRepository).findByCode(currencyCode);
    }

    @Test
    @DisplayName("Should throw exception when currency code not found")
    void shouldThrowExceptionWhenCurrencyCodeNotFound() {
        // Given
        String currencyCode = "XXX";
        given(currencyRepository.findByCode(currencyCode)).willReturn(Optional.empty());

        // When & Then
        assertThrows(CurrencyNotFoundException.class, () -> {
            currencyService.getCurrencyByCode(currencyCode);
        });
        verify(currencyRepository).findByCode(currencyCode);
    }

    @Test
    @DisplayName("Should return currencies by country")
    void shouldReturnCurrenciesByCountry() {
        // Given
        String country = "United States";
        given(currencyRepository.findByCountry(country)).willReturn(usCurrencies);
        given(currencyMapper.mapToDto(usdCurrency)).willReturn(usdCurrencyDto);

        // When
        List<CurrencyDto> result = currencyService.getCurrenciesByCountry(country);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(usdCurrencyDto);
        verify(currencyRepository).findByCountry(country);
    }

    @Test
    @DisplayName("Should return empty list when no currencies found for country")
    void shouldReturnEmptyListWhenNoCurrenciesFoundForCountry() {
        // Given
        String country = "Unknown Country";
        given(currencyRepository.findByCountry(country)).willReturn(Collections.emptyList());

        // When
        List<CurrencyDto> result = currencyService.getCurrenciesByCountry(country);

        // Then
        assertThat(result).isEmpty();
        verify(currencyRepository).findByCountry(country);
    }
}
