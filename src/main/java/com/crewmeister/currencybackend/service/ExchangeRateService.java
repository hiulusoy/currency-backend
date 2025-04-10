package com.crewmeister.currencybackend.service;

import com.crewmeister.currencybackend.dto.request.ConversionRequestDto;
import com.crewmeister.currencybackend.dto.response.ConversionResponseDto;
import com.crewmeister.currencybackend.dto.ExchangeRateDto;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for exchange rate operations
 */
public interface ExchangeRateService {

    /**
     * Get all exchange rates
     *
     * @return List of all exchange rates
     */
    List<ExchangeRateDto> getAllRates();

    /**
     * Get exchange rates for a specific date
     *
     * @param date Date to get rates for
     * @return List of exchange rates on the specified date
     */
    List<ExchangeRateDto> getRatesByDate(LocalDate date);

    /**
     * Get exchange rate for a specific currency and date
     *
     * @param currencyCode Currency code
     * @param date         Date to get rate for
     * @return Exchange rate for the specified currency and date
     */
    ExchangeRateDto getRateByCurrencyAndDate(String currencyCode, LocalDate date);

    /**
     * Convert amount from a currency to EUR on a specific date
     *
     * @param request Conversion request containing source currency, amount, and date
     * @return Conversion response with converted amount
     */
    ConversionResponseDto convertToEur(ConversionRequestDto request);

}
