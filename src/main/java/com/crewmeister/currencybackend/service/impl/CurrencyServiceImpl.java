package com.crewmeister.currencybackend.service.impl;

import com.crewmeister.currencybackend.dto.CurrencyDto;
import com.crewmeister.currencybackend.entity.Currency;
import com.crewmeister.currencybackend.exception.CurrencyNotFoundException;
import com.crewmeister.currencybackend.mapper.CurrencyMapper;
import com.crewmeister.currencybackend.repository.CurrencyRepository;
import com.crewmeister.currencybackend.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the CurrencyService interface for managing currency-related operations.
 * <p>
 * This service provides methods to retrieve currency information from the database,
 * with logging and error handling for various currency retrieval scenarios.
 *
 * @author hiulusoy
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CurrencyServiceImpl implements CurrencyService {

    /**
     * Repository for performing currency-related database operations
     */
    private final CurrencyRepository currencyRepository;

    /**
     * Mapper for converting Currency entities to CurrencyDto objects
     */
    private final CurrencyMapper currencyMapper;

    /**
     * Retrieves all currencies from the database.
     *
     * @return A list of all currencies converted to CurrencyDto objects
     */
    @Override
    public List<CurrencyDto> getAllCurrencies() {
        log.info("Getting all currencies");
        return currencyRepository.findAll().stream()
                .map(currencyMapper::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all active currencies from the database.
     * <p>
     * An active currency is determined by the 'active' flag in the Currency entity.
     *
     * @return A list of active currencies converted to CurrencyDto objects
     */
    @Override
    public List<CurrencyDto> getActiveCurrencies() {
        log.info("Getting active currencies");
        return currencyRepository.findByActiveTrue().stream()
                .map(currencyMapper::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a specific currency by its unique code.
     *
     * @param code The unique identifier of the currency
     * @return The CurrencyDto for the specified currency code
     * @throws CurrencyNotFoundException If no currency is found with the given code
     */
    @Override
    public CurrencyDto getCurrencyByCode(String code) {
        log.info("Getting currency with code: {}", code);
        Currency currency = currencyRepository.findByCode(code)
                .orElseThrow(() -> new CurrencyNotFoundException("Currency not found with code: " + code));
        return currencyMapper.mapToDto(currency);
    }

    /**
     * Retrieves all currencies associated with a specific country.
     *
     * @param country The name of the country
     * @return A list of currencies for the specified country
     */
    @Override
    public List<CurrencyDto> getCurrenciesByCountry(String country) {
        log.info("Getting currencies for country: {}", country);
        return currencyRepository.findByCountry(country).stream()
                .map(currencyMapper::mapToDto)
                .collect(Collectors.toList());
    }
}
