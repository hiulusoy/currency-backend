package com.crewmeister.currencybackend.utils;

import com.crewmeister.currencybackend.dto.ExchangeRateDto;
import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility class for JSON parsing operations related to exchange rates
 */
public class JsonParserUtils {

    /**
     * Extract rates from the JSON structure
     */
    public static List<ExchangeRateDto> extractRatesFromJson(
            JsonNode root,
            String currencyCode,
            Map<Integer, LocalDate> timePeriods,
            Function<String, String> currencyNameResolver) {

        List<ExchangeRateDto> rates = new ArrayList<>();

        // Navigate through SDMX JSON structure
        JsonNode datasets = root.path("data").path("dataSets");
        if (!datasets.isArray() || datasets.size() == 0) {
            return rates;
        }

        JsonNode series = datasets.get(0).path("series");
        Iterator<Map.Entry<String, JsonNode>> seriesFields = series.fields();

        while (seriesFields.hasNext()) {
            Map.Entry<String, JsonNode> seriesEntry = seriesFields.next();
            JsonNode observations = seriesEntry.getValue().path("observations");

            rates.addAll(extractObservations(observations, currencyCode, timePeriods, currencyNameResolver));
        }

        return rates;
    }

    /**
     * Extract observation values from JSON
     */
    public static List<ExchangeRateDto> extractObservations(
            JsonNode observations,
            String currencyCode,
            Map<Integer, LocalDate> timePeriods,
            Function<String, String> currencyNameResolver) {

        List<ExchangeRateDto> rates = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> obsFields = observations.fields();

        while (obsFields.hasNext()) {
            Map.Entry<String, JsonNode> obsEntry = obsFields.next();

            try {
                // Get time period index and rate value
                int timeIndex = Integer.parseInt(obsEntry.getKey());
                JsonNode obsValues = obsEntry.getValue();

                if (obsValues.isArray() && obsValues.size() > 0) {
                    JsonNode valueNode = obsValues.get(0);
                    // Skip if value is null, NaN or not numeric
                    if (valueNode.isNull() || valueNode.asText().equalsIgnoreCase("null")
                            || valueNode.asText().equalsIgnoreCase("nan")
                            || valueNode.asText().equalsIgnoreCase("n/a")) {
                        continue;
                    }

                    // Clean the string value and remove invalid characters
                    String valueText = valueNode.asText().trim();
                    if (valueText.isEmpty()) {
                        continue;
                    }

                    // Convert value to BigDecimal
                    BigDecimal rate;
                    try {
                        rate = new BigDecimal(valueText);
                    } catch (NumberFormatException e) {
                        // Log and skip non-numeric value
                        System.err.println("Skipping non-numeric value: " + valueText + " for currency: " + currencyCode);
                        continue;
                    }

                    LocalDate date = timePeriods.get(timeIndex);

                    if (date != null) {
                        String currencyName = currencyNameResolver.apply(currencyCode);
                        rates.add(ExchangeRateUtils.buildExchangeRateDto(
                                currencyCode, currencyName, date, rate));
                    }
                }
            } catch (Exception e) {
                // Skip this observation if there's an error and continue
                System.err.println("Error processing observation: " + e.getMessage());
                continue;
            }
        }

        return rates;
    }
}
