// ExchangeRateUtils.java
package com.crewmeister.currencybackend.utils;

import com.crewmeister.currencybackend.dto.ExchangeRateDto;
import com.crewmeister.currencybackend.dto.request.ConversionRequestDto;
import com.crewmeister.currencybackend.dto.response.ConversionResponseDto;
import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for exchange rate related operations
 */
public class ExchangeRateUtils {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int DECIMAL_SCALE = 2;

    /**
     * Formats a date to string according to API requirements
     */
    public static String formatDate(LocalDate date) {
        return date.format(DATE_FORMATTER);
    }

    /**
     * Parse date string to LocalDate handling different formats
     */
    public static LocalDate parseDateString(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }

        try {
            if (dateStr.length() == 10) { // YYYY-MM-DD
                return LocalDate.parse(dateStr);
            } else if (dateStr.length() == 7) { // YYYY-MM
                return LocalDate.parse(dateStr + "-01");
            }
        } catch (DateTimeParseException e) {
            // If parsing fails, return null
            return null;
        }

        return null;
    }

    /**
     * Builds the currency key for Bundesbank API
     */
    public static String buildCurrencyKey(String currencyCode, String suffix) {
        return "D." + currencyCode + suffix;
    }

    /**
     * Extract time periods from the SDMX JSON structure
     */
    public static Map<Integer, LocalDate> extractTimePeriods(JsonNode root) {
        Map<Integer, LocalDate> timePeriods = new HashMap<>();

        JsonNode structure = root.path("data").path("structure");
        JsonNode dimensions = structure.path("dimensions").path("observation");

        // Find the time dimension
        for (JsonNode dimension : dimensions) {
            if ("TIME_PERIOD".equals(dimension.path("id").asText())) {
                JsonNode values = dimension.path("values");

                for (int i = 0; i < values.size(); i++) {
                    String dateStr = values.get(i).path("id").asText();
                    timePeriods.put(i, parseDateString(dateStr));
                }
                break;
            }
        }

        return timePeriods;
    }

    /**
     * Build an ExchangeRateDto object
     */
    public static ExchangeRateDto buildExchangeRateDto(
            String currencyCode,
            String currencyName,
            LocalDate date,
            BigDecimal rate) {

        return ExchangeRateDto.builder()
                .currencyCode(currencyCode)
                .currencyName(currencyName)
                .date(date)
                .rate(rate)
                .build();
    }

    /**
     * Converts an amount based on the exchange rate
     */
    public static BigDecimal convertAmount(BigDecimal amount, BigDecimal rate) {
        return amount.divide(rate, DECIMAL_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Builds a conversion response DTO
     */
    public static ConversionResponseDto buildConversionResponse(
            ConversionRequestDto request,
            ExchangeRateDto exchangeRate,
            BigDecimal convertedAmount) {

        return ConversionResponseDto.builder()
                .amount(request.getAmount())
                .fromCurrency(request.getFromCurrency())
                .convertedAmount(convertedAmount)
                .toCurrency("EUR")
                .exchangeRate(exchangeRate.getRate())
                .date(request.getDate())
                .build();
    }
}
