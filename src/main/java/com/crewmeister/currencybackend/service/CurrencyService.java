package com.crewmeister.currencybackend.service;

import com.crewmeister.currencybackend.dto.CurrencyDto;

import java.util.List;

public interface CurrencyService {

    /**
     * Get all currencies
     *
     * @return List of all currencies
     */
    List<CurrencyDto> getAllCurrencies();

    /**
     * Get all active currencies
     *
     * @return List of active currencies
     */
    List<CurrencyDto> getActiveCurrencies();

    /**
     * Get currency by code
     *
     * @param code Currency code
     * @return Currency details
     */
    CurrencyDto getCurrencyByCode(String code);

    /**
     * Get currencies by country
     *
     * @param country Country name
     * @return List of currencies for the specified country
     */
    List<CurrencyDto> getCurrenciesByCountry(String country);
}
