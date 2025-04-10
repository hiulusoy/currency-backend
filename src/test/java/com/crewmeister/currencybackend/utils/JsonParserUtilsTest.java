package com.crewmeister.currencybackend.utils;

import com.crewmeister.currencybackend.dto.ExchangeRateDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class JsonParserUtilsTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("extractRatesFromJson should parse valid JSON data")
    void extractRatesFromJsonShouldParseValidData() throws Exception {
        // Given
        String json = "{ \"data\": { \"dataSets\": [{ \"series\": { \"0:0:0:0:0\": { \"observations\": { \"0\": [1.25] } } } }] } }";
        JsonNode root = objectMapper.readTree(json);
        
        String currencyCode = "USD";
        LocalDate testDate = LocalDate.of(2025, 4, 9);
        
        Map<Integer, LocalDate> timePeriods = new HashMap<>();
        timePeriods.put(0, testDate);
        
        Function<String, String> currencyNameResolver = code -> "US Dollar";
        
        // When
        List<ExchangeRateDto> result = JsonParserUtils.extractRatesFromJson(root, currencyCode, timePeriods, currencyNameResolver);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        
        ExchangeRateDto rate = result.get(0);
        assertEquals(currencyCode, rate.getCurrencyCode());
        assertEquals("US Dollar", rate.getCurrencyName());
        assertEquals(testDate, rate.getDate());
        assertEquals(new BigDecimal("1.25"), rate.getRate());
    }

    @Test
    @DisplayName("extractRatesFromJson should return empty list for missing datasets")
    void extractRatesFromJsonShouldHandleMissingDatasets() throws Exception {
        // Given
        String json = "{ \"data\": { \"dataSets\": [] } }";
        JsonNode root = objectMapper.readTree(json);
        
        String currencyCode = "USD";
        Map<Integer, LocalDate> timePeriods = new HashMap<>();
        Function<String, String> currencyNameResolver = code -> "US Dollar";
        
        // When
        List<ExchangeRateDto> result = JsonParserUtils.extractRatesFromJson(root, currencyCode, timePeriods, currencyNameResolver);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("extractObservations should parse valid observation data")
    void extractObservationsShouldParseValidData() throws Exception {
        // Given
        String json = "{ \"0\": [1.25], \"1\": [1.26] }";
        JsonNode observations = objectMapper.readTree(json);
        
        String currencyCode = "USD";
        LocalDate date1 = LocalDate.of(2025, 4, 9);
        LocalDate date2 = LocalDate.of(2025, 4, 10);
        
        Map<Integer, LocalDate> timePeriods = new HashMap<>();
        timePeriods.put(0, date1);
        timePeriods.put(1, date2);
        
        Function<String, String> currencyNameResolver = code -> "US Dollar";
        
        // When
        List<ExchangeRateDto> result = JsonParserUtils.extractObservations(observations, currencyCode, timePeriods, currencyNameResolver);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        assertEquals(date1, result.get(0).getDate());
        assertEquals(new BigDecimal("1.25"), result.get(0).getRate());
        
        assertEquals(date2, result.get(1).getDate());
        assertEquals(new BigDecimal("1.26"), result.get(1).getRate());
    }

    @Test
    @DisplayName("extractObservations should skip invalid numeric values")
    void extractObservationsShouldSkipInvalidNumericValues() throws Exception {
        // Given
        String json = "{ \"0\": [1.25], \"1\": [\"NaN\"], \"2\": [\"null\"], \"3\": [\"N/A\"], \"4\": [\"\"], \"5\": [\"invalid\"] }";
        JsonNode observations = objectMapper.readTree(json);
        
        String currencyCode = "USD";
        LocalDate date = LocalDate.of(2025, 4, 9);
        
        Map<Integer, LocalDate> timePeriods = new HashMap<>();
        for (int i = 0; i < 6; i++) {
            timePeriods.put(i, date);
        }
        
        Function<String, String> currencyNameResolver = code -> "US Dollar";
        
        // When
        List<ExchangeRateDto> result = JsonParserUtils.extractObservations(observations, currencyCode, timePeriods, currencyNameResolver);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size()); // Only one valid numeric value
        assertEquals(new BigDecimal("1.25"), result.get(0).getRate());
    }

    @Test
    @DisplayName("extractObservations should handle missing time periods")
    void extractObservationsShouldHandleMissingTimePeriods() throws Exception {
        // Given
        String json = "{ \"0\": [1.25], \"1\": [1.26], \"2\": [1.27] }";
        JsonNode observations = objectMapper.readTree(json);
        
        String currencyCode = "USD";
        LocalDate date = LocalDate.of(2025, 4, 9);
        
        Map<Integer, LocalDate> timePeriods = new HashMap<>();
        timePeriods.put(0, date); // Only provide mapping for index 0
        
        Function<String, String> currencyNameResolver = code -> "US Dollar";
        
        // When
        List<ExchangeRateDto> result = JsonParserUtils.extractObservations(observations, currencyCode, timePeriods, currencyNameResolver);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size()); // Only one valid time period
        assertEquals(date, result.get(0).getDate());
        assertEquals(new BigDecimal("1.25"), result.get(0).getRate());
    }

    @Test
    @DisplayName("extractObservations should handle observation arrays of different sizes")
    void extractObservationsShouldHandleDifferentArraySizes() throws Exception {
        // Given
        String json = "{ \"0\": [1.25], \"1\": [], \"2\": [1.27, \"extra\"] }";
        JsonNode observations = objectMapper.readTree(json);
        
        String currencyCode = "USD";
        LocalDate date = LocalDate.of(2025, 4, 9);
        
        Map<Integer, LocalDate> timePeriods = new HashMap<>();
        for (int i = 0; i < 3; i++) {
            timePeriods.put(i, date);
        }
        
        Function<String, String> currencyNameResolver = code -> "US Dollar";
        
        // When
        List<ExchangeRateDto> result = JsonParserUtils.extractObservations(observations, currencyCode, timePeriods, currencyNameResolver);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size()); // Two valid observations (index 0 and 2)
    }

    @Test
    @DisplayName("extractObservations should not throw exceptions for invalid data")
    void extractObservationsShouldNotThrowExceptionsForInvalidData() throws Exception {
        // Given
        String json = "{ \"invalid\": [1.25], \"0\": [1.25] }";
        JsonNode observations = objectMapper.readTree(json);
        
        String currencyCode = "USD";
        LocalDate date = LocalDate.of(2025, 4, 9);
        
        Map<Integer, LocalDate> timePeriods = new HashMap<>();
        timePeriods.put(0, date);
        
        Function<String, String> currencyNameResolver = code -> "US Dollar";
        
        // When & Then
        // This should not throw an exception
        List<ExchangeRateDto> result = JsonParserUtils.extractObservations(observations, currencyCode, timePeriods, currencyNameResolver);
        
        assertNotNull(result);
        assertEquals(1, result.size()); // Should still process the valid observation
    }
}
