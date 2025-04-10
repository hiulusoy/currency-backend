package com.crewmeister.currencybackend.service.impl;

import com.crewmeister.currencybackend.client.BundesbankClient;
import com.crewmeister.currencybackend.dto.ExchangeRateDto;
import com.crewmeister.currencybackend.dto.request.ConversionRequestDto;
import com.crewmeister.currencybackend.dto.response.ConversionResponseDto;
import com.crewmeister.currencybackend.exception.ExchangeRateNotFoundException;
import com.crewmeister.currencybackend.exception.ExternalServiceException;
import com.crewmeister.currencybackend.service.CurrencyService;
import com.crewmeister.currencybackend.service.ExchangeRateService;
import com.crewmeister.currencybackend.utils.ExchangeRateUtils;
import com.crewmeister.currencybackend.utils.JsonParserUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service implementation for managing exchange rates using the Bundesbank API.
 * <p>
 * This service provides methods to retrieve exchange rates for various currencies,
 * convert between currencies, and handle rate-related operations.
 * <p>
 * Key Features:
 * - Fetches exchange rates for multiple currencies
 * - Supports date-based rate retrieval
 * - Handles currency conversions
 * - Provides fallback mechanisms for API interactions
 *
 * @author hiulusoy
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateServiceImpl implements ExchangeRateService {

    /**
     * List of default currencies to fetch exchange rates for
     */
    private static final List<String> DEFAULT_CURRENCIES = Arrays.asList("USD", "EUR", "GBP", "JPY", "CHF");

    /**
     * Default number of days to retrieve historical exchange rates
     */
    private static final int DEFAULT_DAYS_RANGE = 30;

    /**
     * Default format for API responses
     */
    private static final String DEFAULT_FORMAT = "json";

    /**
     * Default language for API responses
     */
    private static final String DEFAULT_LANGUAGE = "en";

    /**
     * Bundesbank API dataflow identifier
     */
    @Value("${bundesbank.api.dataflow-id}")
    private String dataflowId;

    /**
     * Client for interacting with Bundesbank API
     */
    private final BundesbankClient bundesbankClient;

    /**
     * JSON parsing utility
     */
    private final ObjectMapper objectMapper;

    /**
     * Service for retrieving currency information
     */
    private final CurrencyService currencyService;

    /**
     * Retrieves exchange rates for all default currencies for the past 30 days.
     * <p>
     * This method fetches exchange rates for USD, GBP, JPY, CHF against EUR,
     * and includes a fixed rate for EUR/EUR (always 1.0).
     *
     * @return List of ExchangeRateDto containing exchange rates
     * @throws ExternalServiceException if no exchange rates could be fetched
     */
    @Override
    public List<ExchangeRateDto> getAllRates() {
        log.info("Getting all exchange rates");

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(DEFAULT_DAYS_RANGE);

        // Fetch rates for all currencies except EUR
        List<String> filteredCurrencies = DEFAULT_CURRENCIES.stream()
                .filter(curr -> !"EUR".equals(curr))
                .collect(Collectors.toList());

        // Fetch rates for specified currencies
        List<ExchangeRateDto> allRates = fetchRatesForCurrencies(filteredCurrencies, startDate, endDate);

        // Add fixed EUR/EUR rate for each day
        for (int i = 0; i <= DEFAULT_DAYS_RANGE; i++) {
            LocalDate date = endDate.minusDays(i);
            ExchangeRateDto eurRate = createEuroToEuroRate(date);
            allRates.add(eurRate);
        }

        // Validate and return rates
        if (allRates.isEmpty()) {
            throw new ExternalServiceException("No exchange rates could be fetched from the API", null);
        }

        return allRates;
    }

    /**
     * Retrieves exchange rates for all default currencies on a specific date.
     *
     * @param date The date for which to retrieve exchange rates
     * @return List of ExchangeRateDto for the specified date
     * @throws ExchangeRateNotFoundException if no rates are found for the date
     */
    @Override
    public List<ExchangeRateDto> getRatesByDate(LocalDate date) {
        log.info("Getting exchange rates for date: {}", date);

        // Fetch rates for all currencies except EUR
        List<String> filteredCurrencies = DEFAULT_CURRENCIES.stream()
                .filter(curr -> !"EUR".equals(curr))
                .collect(Collectors.toList());

        // Fetch and filter rates for the specific date
        List<ExchangeRateDto> dateRates = fetchRatesForCurrencies(filteredCurrencies, date, date)
                .stream()
                .filter(rate -> rate.getDate().equals(date))
                .collect(Collectors.toList());

        // Add fixed EUR/EUR rate
        dateRates.add(createEuroToEuroRate(date));

        // Validate and return rates
        if (dateRates.isEmpty()) {
            log.warn("No exchange rates found for date: {}", date);
            throw new ExchangeRateNotFoundException("all currencies", date);
        }

        return dateRates;
    }

    /**
     * Retrieves the exchange rate for a specific currency on a given date.
     *
     * @param currencyCode The currency code to retrieve the rate for
     * @param date         The date for which to retrieve the exchange rate
     * @return ExchangeRateDto for the specified currency and date
     * @throws ExchangeRateNotFoundException if no rate is found
     */
    @Override
    public ExchangeRateDto getRateByCurrencyAndDate(String currencyCode, LocalDate date) {
        log.info("Getting exchange rate for currency: {} and date: {}", currencyCode, date);

        // Special handling for EUR/EUR rate
        if ("EUR".equals(currencyCode)) {
            return createEuroToEuroRate(date);
        }

        // Fetch rates for the specific currency
        List<ExchangeRateDto> rates = fetchRatesForCurrency(currencyCode, date, date);

        // Return the rate for the specific date
        return rates.stream()
                .filter(rate -> rate.getDate().equals(date))
                .findFirst()
                .orElseThrow(() -> new ExchangeRateNotFoundException(currencyCode, date));
    }

    /**
     * Converts an amount from a given currency to EUR.
     *
     * @param request Conversion request containing amount, source currency, and date
     * @return ConversionResponseDto with the converted amount in EUR
     */
    @Override
    public ConversionResponseDto convertToEur(ConversionRequestDto request) {
        log.info("Converting {} {} to EUR on date: {}",
                request.getAmount(), request.getFromCurrency(), request.getDate());

        // Get the exchange rate for the source currency
        ExchangeRateDto exchangeRate = getRateByCurrencyAndDate(request.getFromCurrency(), request.getDate());

        // Perform the conversion
        BigDecimal convertedAmount = ExchangeRateUtils.convertAmount(request.getAmount(), exchangeRate.getRate());

        // Build and return the conversion response
        return ExchangeRateUtils.buildConversionResponse(request, exchangeRate, convertedAmount);
    }

    // =============== Private Helper Methods ===============

    /**
     * Fetches exchange rates for multiple currencies within a specified date range.
     *
     * @param currencies List of currency codes to fetch rates for
     * @param startDate  Start date of the range
     * @param endDate    End date of the range
     * @return List of ExchangeRateDto for the specified currencies and date range
     */
    private List<ExchangeRateDto> fetchRatesForCurrencies(List<String> currencies, LocalDate startDate, LocalDate endDate) {
        List<ExchangeRateDto> allRates = new ArrayList<>();

        for (String currency : currencies) {
            try {
                // Fetch rates for each currency
                List<ExchangeRateDto> currencyRates = fetchRatesForCurrency(currency, startDate, endDate);
                allRates.addAll(currencyRates);
            } catch (Exception e) {
                // Log and skip currencies with fetch errors
                log.error("Error fetching rates for currency: " + currency, e);
            }
        }

        return allRates;
    }

    /**
     * Fetches exchange rates for a single currency within a specified date range.
     *
     * @param currencyCode Currency code to fetch rates for
     * @param startDate    Start date of the range
     * @param endDate      End date of the range
     * @return List of ExchangeRateDto for the specified currency and date range
     */
    private List<ExchangeRateDto> fetchRatesForCurrency(String currencyCode, LocalDate startDate, LocalDate endDate) {
        // Construct the currency key for Bundesbank API
        String key = ExchangeRateUtils.buildCurrencyKey(currencyCode, ".EUR.BB.AC.000");
        String startDateStr = ExchangeRateUtils.formatDate(startDate);
        String endDateStr = ExchangeRateUtils.formatDate(endDate);

        try {
            // Fetch raw JSON data from Bundesbank API
            String ratesJson = bundesbankClient.getDataForDateRange(
                    dataflowId,
                    key,
                    DEFAULT_FORMAT,
                    DEFAULT_LANGUAGE,
                    startDateStr,
                    endDateStr);

            // Parse the JSON response
            return parseExchangeRatesForCurrency(currencyCode, ratesJson);
        } catch (feign.FeignException.NotFound e) {
            // Handle cases where no data is found for the currency
            log.warn("No data found for currency {} in date range {} to {}",
                    currencyCode, startDateStr, endDateStr);
            return new ArrayList<>();
        }
    }

    /**
     * Creates a fixed ExchangeRateDto for EUR/EUR (always 1.0).
     *
     * @param date The date for the EUR rate
     * @return ExchangeRateDto with a rate of 1.0 for EUR
     */
    private ExchangeRateDto createEuroToEuroRate(LocalDate date) {
        String currencyName = getCurrencyName("EUR");
        return ExchangeRateDto.builder()
                .currencyCode("EUR")
                .currencyName(currencyName)
                .date(date)
                .rate(BigDecimal.ONE) // EUR/EUR rate is always 1.0
                .build();
    }

    /**
     * Parses JSON response from Bundesbank API to extract exchange rates.
     *
     * @param currencyCode Currency code to parse rates for
     * @param json         Raw JSON response from the API
     * @return List of ExchangeRateDto parsed from the JSON
     * @throws ExternalServiceException if parsing fails
     */
    private List<ExchangeRateDto> parseExchangeRatesForCurrency(String currencyCode, String json) {
        try {
            // Parse the JSON
            JsonNode root = objectMapper.readTree(json);

            // Extract time periods
            Map<Integer, LocalDate> timePeriods = ExchangeRateUtils.extractTimePeriods(root);

            // Extract and return rates
            return JsonParserUtils.extractRatesFromJson(root, currencyCode, timePeriods, this::getCurrencyName);

        } catch (Exception e) {
            log.error("Error parsing exchange rates JSON for " + currencyCode, e);
            throw new ExternalServiceException("Failed to parse exchange rates: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves the full name of a currency.
     *
     * @param currencyCode Currency code to get the name for
     * @return Currency name or the currency code if name cannot be found
     */
    private String getCurrencyName(String currencyCode) {
        try {
            return currencyService.getCurrencyByCode(currencyCode).getName();
        } catch (Exception e) {
            // Fallback to currency code if name cannot be retrieved
            return currencyCode;
        }
    }
}
