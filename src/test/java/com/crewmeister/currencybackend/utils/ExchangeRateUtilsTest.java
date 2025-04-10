package com.crewmeister.currencybackend.utils;

import com.crewmeister.currencybackend.dto.ExchangeRateDto;
import com.crewmeister.currencybackend.dto.request.ConversionRequestDto;
import com.crewmeister.currencybackend.dto.response.ConversionResponseDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ExchangeRateUtilsTest {

    private static final LocalDate TEST_DATE = LocalDate.of(2025, 4, 9);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("formatDate should properly format LocalDate objects")
    void formatDateShouldProperlyFormatDates() {
        // Given
        LocalDate date = TEST_DATE;
        
        // When
        String result = ExchangeRateUtils.formatDate(date);
        
        // Then
        assertEquals("2025-04-09", result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"2025-04-09", "2025-04"})
    @DisplayName("parseDateString should correctly parse valid date strings")
    void parseDateStringShouldParseValidDates(String dateStr) {
        // When
        LocalDate result = ExchangeRateUtils.parseDateString(dateStr);
        
        // Then
        assertNotNull(result);
        if (dateStr.length() == 10) {
            assertEquals(LocalDate.of(2025, 4, 9), result);
        } else {
            assertEquals(LocalDate.of(2025, 4, 1), result);
        }
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"invalid", "2025"})
    @DisplayName("parseDateString should handle invalid or null inputs")
    void parseDateStringShouldHandleInvalidInputs(String dateStr) {
        // When
        LocalDate result = ExchangeRateUtils.parseDateString(dateStr);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("buildCurrencyKey should correctly build the currency key")
    void buildCurrencyKeyShouldBuildCorrectKey() {
        // Given
        String currencyCode = "USD";
        String suffix = ".BBEX3.D";
        
        // When
        String result = ExchangeRateUtils.buildCurrencyKey(currencyCode, suffix);
        
        // Then
        assertEquals("D.USD.BBEX3.D", result);
    }

    @Test
    @DisplayName("extractTimePeriods should correctly extract time periods from JSON")
    void extractTimePeriodsShouldExtractFromJson() throws Exception {
        // Given
        String json = "{ \"data\": { \"structure\": { \"dimensions\": { \"observation\": [" +
                      "{ \"id\": \"TIME_PERIOD\", \"values\": [" +
                      "{ \"id\": \"2025-04-09\" }, { \"id\": \"2025-04-10\" }" +
                      "] }] } } } }";
        JsonNode root = objectMapper.readTree(json);
        
        // When
        Map<Integer, LocalDate> result = ExchangeRateUtils.extractTimePeriods(root);
        
        // Then
        assertEquals(2, result.size());
        assertEquals(LocalDate.of(2025, 4, 9), result.get(0));
        assertEquals(LocalDate.of(2025, 4, 10), result.get(1));
    }

    @Test
    @DisplayName("buildExchangeRateDto should correctly build a DTO")
    void buildExchangeRateDtoShouldBuildCorrectDto() {
        // Given
        String currencyCode = "USD";
        String currencyName = "US Dollar";
        LocalDate date = TEST_DATE;
        BigDecimal rate = new BigDecimal("0.92");
        
        // When
        ExchangeRateDto result = ExchangeRateUtils.buildExchangeRateDto(currencyCode, currencyName, date, rate);
        
        // Then
        assertNotNull(result);
        assertEquals(currencyCode, result.getCurrencyCode());
        assertEquals(currencyName, result.getCurrencyName());
        assertEquals(date, result.getDate());
        assertEquals(rate, result.getRate());
    }

    @ParameterizedTest
    @MethodSource("provideAmountsAndRates")
    @DisplayName("convertAmount should correctly convert currency based on the exchange rate")
    void convertAmountShouldCorrectlyConvertCurrency(BigDecimal amount, BigDecimal rate, BigDecimal expected) {
        // When
        BigDecimal result = ExchangeRateUtils.convertAmount(amount, rate);
        
        // Then
        assertEquals(0, expected.compareTo(result), 
                "Converting " + amount + " with rate " + rate + " should equal " + expected);
    }

    private static Stream<Arguments> provideAmountsAndRates() {
        return Stream.of(
            Arguments.of(new BigDecimal("100"), new BigDecimal("0.92"), new BigDecimal("108.70")),
            Arguments.of(new BigDecimal("10000"), new BigDecimal("0.006164"), new BigDecimal("1622323.17")),
            Arguments.of(new BigDecimal("1"), new BigDecimal("1"), new BigDecimal("1.00")),
            Arguments.of(new BigDecimal("0"), new BigDecimal("0.5"), new BigDecimal("0.00"))
        );
    }

    @Test
    @DisplayName("buildConversionResponse should correctly build a response DTO")
    void buildConversionResponseShouldBuildCorrectResponse() {
        // Given
        ConversionRequestDto request = ConversionRequestDto.builder()
                .fromCurrency("USD")
                .amount(new BigDecimal("100"))
                .date(TEST_DATE)
                .build();
                
        ExchangeRateDto exchangeRate = ExchangeRateDto.builder()
                .currencyCode("USD")
                .currencyName("US Dollar")
                .date(TEST_DATE)
                .rate(new BigDecimal("0.92"))
                .build();
                
        BigDecimal convertedAmount = new BigDecimal("108.70");
        
        // When
        ConversionResponseDto result = ExchangeRateUtils.buildConversionResponse(request, exchangeRate, convertedAmount);
        
        // Then
        assertNotNull(result);
        assertEquals(request.getFromCurrency(), result.getFromCurrency());
        assertEquals("EUR", result.getToCurrency());
        assertEquals(request.getAmount(), result.getAmount());
        assertEquals(exchangeRate.getRate(), result.getExchangeRate());
        assertEquals(request.getDate(), result.getDate());
        assertEquals(convertedAmount, result.getConvertedAmount());
    }

    @Test
    @DisplayName("convertAmount should handle division by zero gracefully")
    void convertAmountShouldHandleDivisionByZero() {
        // Given
        BigDecimal amount = new BigDecimal("100");
        BigDecimal rate = BigDecimal.ZERO;
        
        // When & Then
        assertThrows(ArithmeticException.class, () -> 
            ExchangeRateUtils.convertAmount(amount, rate));
    }
}
