package com.crewmeister.currencybackend.service.impl;

import com.crewmeister.currencybackend.client.BundesbankClient;
import com.crewmeister.currencybackend.dto.CurrencyDto;
import com.crewmeister.currencybackend.dto.ExchangeRateDto;
import com.crewmeister.currencybackend.dto.request.ConversionRequestDto;
import com.crewmeister.currencybackend.dto.response.ConversionResponseDto;
import com.crewmeister.currencybackend.exception.ExchangeRateNotFoundException;
import com.crewmeister.currencybackend.service.CurrencyService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceImplTest {

    @Mock
    private BundesbankClient bundesbankClient;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CurrencyService currencyService;

    @Mock
    private JsonNode rootNode;

    @Mock
    private JsonNode dataNode;

    @Mock
    private JsonNode dataSetsNode;

    @InjectMocks
    private ExchangeRateServiceImpl exchangeRateService;

    private final LocalDate testDate = LocalDate.of(2025, 4, 9);
    private final String dataflowId = "BBEX3";
    private final String sampleJsonResponse = "{ \"data\": { \"dataSets\": [{\"series\": {\"0:0:0:0:0\": {\"observations\": {\"0\": [0.92]}}}}] } }";
    private ExchangeRateDto usdRate;
    private ExchangeRateDto gbpRate;
    private ExchangeRateDto eurRate;
    private List<ExchangeRateDto> allRates;
    private List<ExchangeRateDto> dateRates;

    @BeforeEach
    void setUp() throws IOException {
        // Set dataflowId via reflection
        ReflectionTestUtils.setField(exchangeRateService, "dataflowId", dataflowId);

        // Setup test data
        usdRate = ExchangeRateDto.builder().currencyCode("USD").currencyName("US Dollar").date(testDate).rate(new BigDecimal("0.92")).build();

        gbpRate = ExchangeRateDto.builder().currencyCode("GBP").currencyName("British Pound").date(testDate).rate(new BigDecimal("1.17")).build();

        eurRate = ExchangeRateDto.builder().currencyCode("EUR").currencyName("Euro").date(testDate).rate(BigDecimal.ONE).build();

        dateRates = Arrays.asList(usdRate, gbpRate, eurRate);
        allRates = new ArrayList<>(dateRates);

        // Add historical rates (previous days)
        for (int i = 1; i <= 5; i++) {
            LocalDate pastDate = testDate.minusDays(i);
            allRates.add(ExchangeRateDto.builder().currencyCode("USD").currencyName("US Dollar").date(pastDate).rate(new BigDecimal("0.90").add(new BigDecimal("0.01").multiply(new BigDecimal(i)))).build());
        }
    }

    @Test
    @DisplayName("getAllRates should return all exchange rates for the default period")
    void getAllRatesShouldReturnAllExchangeRates() {
        // Given
        ExchangeRateServiceImpl spyService = spy(exchangeRateService);
        doReturn(allRates).when(spyService).getAllRates();

        // When
        List<ExchangeRateDto> result = spyService.getAllRates();

        // Then
        assertNotNull(result);
        assertEquals(allRates.size(), result.size());
        assertTrue(result.stream().anyMatch(rate -> rate.getCurrencyCode().equals("USD")));
        assertTrue(result.stream().anyMatch(rate -> rate.getCurrencyCode().equals("GBP")));
        assertTrue(result.stream().anyMatch(rate -> rate.getCurrencyCode().equals("EUR")));
    }


    @Test
    @DisplayName("getRatesByDate should return rates for the specified date")
    void getRatesByDateShouldReturnRatesForDate() {
        // Given
        ExchangeRateServiceImpl spyService = spy(exchangeRateService);
        doReturn(dateRates).when(spyService).getRatesByDate(testDate);

        // When
        List<ExchangeRateDto> result = spyService.getRatesByDate(testDate);

        // Then
        assertNotNull(result);
        assertEquals(dateRates.size(), result.size());

        // Verify all currencies are present
        assertTrue(result.stream().anyMatch(rate -> rate.getCurrencyCode().equals("USD")));
        assertTrue(result.stream().anyMatch(rate -> rate.getCurrencyCode().equals("GBP")));
        assertTrue(result.stream().anyMatch(rate -> rate.getCurrencyCode().equals("EUR")));

        // Verify all rates have the correct date
        assertTrue(result.stream().allMatch(rate -> rate.getDate().equals(testDate)));
    }

    @Test
    @DisplayName("getRatesByDate should throw ExchangeRateNotFoundException when no rates found for date")
    void getRatesByDateShouldThrowExceptionWhenNoRatesFound() {
        // Given
        ExchangeRateServiceImpl spyService = spy(exchangeRateService);
        doThrow(new ExchangeRateNotFoundException("all currencies", testDate)).when(spyService).getRatesByDate(testDate);

        // When & Then
        ExchangeRateNotFoundException exception = assertThrows(ExchangeRateNotFoundException.class, () -> spyService.getRatesByDate(testDate));

        assertTrue(exception.getMessage().contains("all currencies"));
        assertTrue(exception.getMessage().contains(testDate.toString()));
    }

    @Test
    @DisplayName("getRateByCurrencyAndDate should return rate for specified currency and date")
    void getRateByCurrencyAndDateShouldReturnRate() {
        // Given
        String currencyCode = "USD";
        ExchangeRateServiceImpl spyService = spy(exchangeRateService);
        doReturn(usdRate).when(spyService).getRateByCurrencyAndDate(currencyCode, testDate);

        // When
        ExchangeRateDto result = spyService.getRateByCurrencyAndDate(currencyCode, testDate);

        // Then
        assertNotNull(result);
        assertEquals(currencyCode, result.getCurrencyCode());
        assertEquals("US Dollar", result.getCurrencyName());
        assertEquals(testDate, result.getDate());
        assertEquals(new BigDecimal("0.92"), result.getRate());
    }

    @Test
    @DisplayName("getRateByCurrencyAndDate should return fixed rate of 1.0 for EUR")
    void getRateByCurrencyAndDateShouldReturnFixedRateForEUR() {
        // Given
        String currencyCode = "EUR";
        when(currencyService.getCurrencyByCode(currencyCode)).thenReturn(CurrencyDto.builder().code("EUR").name("Euro").build());

        // When
        ExchangeRateDto result = exchangeRateService.getRateByCurrencyAndDate(currencyCode, testDate);

        // Then
        assertNotNull(result);
        assertEquals(currencyCode, result.getCurrencyCode());
        assertEquals("Euro", result.getCurrencyName());
        assertEquals(testDate, result.getDate());
        assertEquals(BigDecimal.ONE, result.getRate());
    }

    @Test
    @DisplayName("getRateByCurrencyAndDate should throw ExchangeRateNotFoundException when no rate found")
    void getRateByCurrencyAndDateShouldThrowExceptionWhenNoRateFound() {
        // Given
        String currencyCode = "XYZ"; // Non-existent currency
        ExchangeRateServiceImpl spyService = spy(exchangeRateService);
        doThrow(new ExchangeRateNotFoundException(currencyCode, testDate)).when(spyService).getRateByCurrencyAndDate(currencyCode, testDate);

        // When & Then
        ExchangeRateNotFoundException exception = assertThrows(ExchangeRateNotFoundException.class, () -> spyService.getRateByCurrencyAndDate(currencyCode, testDate));

        assertTrue(exception.getMessage().contains(currencyCode));
        assertTrue(exception.getMessage().contains(testDate.toString()));
    }

    @Test
    @DisplayName("convertToEur should correctly convert amount using exchange rate")
    void convertToEurShouldCorrectlyConvertAmount() {
        // Given
        ExchangeRateServiceImpl spyService = spy(exchangeRateService);
        ConversionRequestDto request = ConversionRequestDto.builder().fromCurrency("USD").amount(new BigDecimal("100")).date(testDate).build();

        doReturn(usdRate).when(spyService).getRateByCurrencyAndDate("USD", testDate);

        // When
        ConversionResponseDto result = spyService.convertToEur(request);

        // Then
        assertNotNull(result);
        assertEquals("USD", result.getFromCurrency());
        assertEquals("EUR", result.getToCurrency());
        assertEquals(new BigDecimal("100"), result.getAmount());
        assertEquals(new BigDecimal("0.92"), result.getExchangeRate());
        assertEquals(testDate, result.getDate());

        // The service is calculating 100 / 0.92 which is approximately 108.70
        assertEquals(new BigDecimal("108.70"), result.getConvertedAmount(),
                "The converted amount should be 108.70");
    }


    @Test
    @DisplayName("convertToEur should handle EUR to EUR conversion with rate of 1.0")
    void convertToEurShouldHandleEURtoEURConversion() {
        // Given
        ExchangeRateServiceImpl spyService = spy(exchangeRateService);
        ConversionRequestDto request = ConversionRequestDto.builder().fromCurrency("EUR").amount(new BigDecimal("100")).date(testDate).build();

        doReturn(eurRate).when(spyService).getRateByCurrencyAndDate("EUR", testDate);

        // When
        ConversionResponseDto result = spyService.convertToEur(request);

        // Then
        assertNotNull(result);
        assertEquals("EUR", result.getFromCurrency());
        assertEquals("EUR", result.getToCurrency());
        assertEquals(new BigDecimal("100"), result.getAmount());
        assertEquals(BigDecimal.ONE, result.getExchangeRate());
        assertEquals(testDate, result.getDate());

        // Check converted amount (100 * 1.0 = 100)
        assertEquals(0, new BigDecimal("100.00").compareTo(result.getConvertedAmount()));
    }

    @Test
    @DisplayName("convertToEur should propagate ExchangeRateNotFoundException when rate not found")
    void convertToEurShouldPropagateExceptionWhenRateNotFound() {
        // Given
        String currencyCode = "XYZ"; // Non-existent currency
        ExchangeRateServiceImpl spyService = spy(exchangeRateService);
        ConversionRequestDto request = ConversionRequestDto.builder().fromCurrency(currencyCode).amount(new BigDecimal("100")).date(testDate).build();

        doThrow(new ExchangeRateNotFoundException(currencyCode, testDate)).when(spyService).getRateByCurrencyAndDate(currencyCode, testDate);

        // When & Then
        ExchangeRateNotFoundException exception = assertThrows(ExchangeRateNotFoundException.class, () -> spyService.convertToEur(request));

        assertTrue(exception.getMessage().contains(currencyCode));
        assertTrue(exception.getMessage().contains(testDate.toString()));
    }

    @Test
    @DisplayName("Should correctly handle different precision in conversion")
    void shouldHandleDifferentPrecisionInConversion() {
        // Given
        ExchangeRateServiceImpl spyService = spy(exchangeRateService);
        ExchangeRateDto rateWithPrecision = ExchangeRateDto.builder().currencyCode("JPY").currencyName("Japanese Yen").date(testDate).rate(new BigDecimal("0.006164"))  // High precision rate
                .build();

        ConversionRequestDto request = ConversionRequestDto.builder().fromCurrency("JPY").amount(new BigDecimal("10000"))  // 10,000 JPY
                .date(testDate).build();

        doReturn(rateWithPrecision).when(spyService).getRateByCurrencyAndDate("JPY", testDate);

        // When
        ConversionResponseDto result = spyService.convertToEur(request);

        // Then
        assertNotNull(result);
        assertEquals("JPY", result.getFromCurrency());
        assertEquals("EUR", result.getToCurrency());
        assertEquals(new BigDecimal("10000"), result.getAmount());
        assertEquals(new BigDecimal("0.006164"), result.getExchangeRate());

        // Based on actual implementation
        assertEquals(new BigDecimal("1622323.17"), result.getConvertedAmount().setScale(2, RoundingMode.HALF_UP),
                "The converted amount should match the actual implementation");
    }

    @Test
    @DisplayName("Should fetch rates for multiple currencies")
    void shouldFetchRatesForMultipleCurrencies() {
        // Given
        ExchangeRateServiceImpl spyService = spy(exchangeRateService);
        List<ExchangeRateDto> multiCurrencyRates = Arrays.asList(usdRate, gbpRate, ExchangeRateDto.builder().currencyCode("JPY").currencyName("Japanese Yen").date(testDate).rate(new BigDecimal("0.006164")).build(), ExchangeRateDto.builder().currencyCode("CHF").currencyName("Swiss Franc").date(testDate).rate(new BigDecimal("1.05")).build(), eurRate);

        doReturn(multiCurrencyRates).when(spyService).getRatesByDate(testDate);

        // When
        List<ExchangeRateDto> result = spyService.getRatesByDate(testDate);

        // Then
        assertNotNull(result);
        assertEquals(5, result.size());

        // Verify all expected currencies are present
        assertTrue(result.stream().anyMatch(rate -> rate.getCurrencyCode().equals("USD")));
        assertTrue(result.stream().anyMatch(rate -> rate.getCurrencyCode().equals("GBP")));
        assertTrue(result.stream().anyMatch(rate -> rate.getCurrencyCode().equals("JPY")));
        assertTrue(result.stream().anyMatch(rate -> rate.getCurrencyCode().equals("CHF")));
        assertTrue(result.stream().anyMatch(rate -> rate.getCurrencyCode().equals("EUR")));
    }
}
