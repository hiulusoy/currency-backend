package com.crewmeister.currencybackend.repository;

import com.crewmeister.currencybackend.entity.Currency;
import com.crewmeister.currencybackend.entity.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for performing database operations on ExchangeRate entities.
 * Key Features:
 * - Retrieve exchange rates by date, currency, and date ranges
 * - Find latest exchange rates
 * - Delete historical exchange rates
 *
 * @author [Your Name]
 * @version 1.0
 * @since [Current Date]
 */
@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    /**
     * Retrieves all exchange rates for a specific date.
     *
     * @param date The date for which to fetch exchange rates
     * @return List of ExchangeRate entities for the specified date
     */
    List<ExchangeRate> findByRateDate(LocalDate date);

    /**
     * Finds an exchange rate for a specific currency and date.
     *
     * @param currency The Currency entity to find the rate for
     * @param date     The specific date of the exchange rate
     * @return Optional containing the ExchangeRate if found
     */
    Optional<ExchangeRate> findByCurrencyAndRateDate(Currency currency, LocalDate date);

    /**
     * Finds an exchange rate for a specific currency code and date.
     *
     * @param currencyCode The currency code (e.g., "USD", "EUR")
     * @param date         The specific date of the exchange rate
     * @return Optional containing the ExchangeRate if found
     */
    Optional<ExchangeRate> findByCurrencyCodeAndRateDate(String currencyCode, LocalDate date);

    /**
     * Retrieves all exchange rates for a specific currency.
     *
     * @param currency The Currency entity to retrieve rates for
     * @return List of ExchangeRate entities for the specified currency
     */
    List<ExchangeRate> findByCurrency(Currency currency);

    /**
     * Retrieves all exchange rates for a specific currency code.
     *
     * @param currencyCode The currency code (e.g., "USD", "EUR")
     * @return List of ExchangeRate entities for the specified currency code
     */
    List<ExchangeRate> findByCurrencyCode(String currencyCode);

    /**
     * Finds exchange rates for a specific currency within a date range.
     *
     * @param currency  The Currency entity to retrieve rates for
     * @param startDate The start date of the range (inclusive)
     * @param endDate   The end date of the range (inclusive)
     * @return List of ExchangeRate entities within the specified date range
     */
    List<ExchangeRate> findByCurrencyAndRateDateBetween(Currency currency, LocalDate startDate, LocalDate endDate);

    /**
     * Finds exchange rates for a specific currency code within a date range.
     *
     * @param currencyCode The currency code (e.g., "USD", "EUR")
     * @param startDate    The start date of the range (inclusive)
     * @param endDate      The end date of the range (inclusive)
     * @return List of ExchangeRate entities within the specified date range
     */
    List<ExchangeRate> findByCurrencyCodeAndRateDateBetween(String currencyCode, LocalDate startDate, LocalDate endDate);

    /**
     * Retrieves the latest exchange rates for all currencies.
     * <p>
     * Finds all exchange rates with the most recent date in the database.
     *
     * @return List of the most recent ExchangeRate entities
     */
    @Query("SELECT er FROM ExchangeRate er WHERE er.rateDate = (SELECT MAX(e.rateDate) FROM ExchangeRate e)")
    List<ExchangeRate> findLatestRates();

    /**
     * Retrieves the latest exchange rate for a specific currency.
     *
     * @param currencyCode The currency code to find the latest rate for
     * @return Optional containing the most recent ExchangeRate for the specified currency
     */
    @Query("SELECT er FROM ExchangeRate er WHERE er.currency.code = :currencyCode AND er.rateDate = (SELECT MAX(e.rateDate) FROM ExchangeRate e WHERE e.currency.code = :currencyCode)")
    Optional<ExchangeRate> findLatestRateByCurrencyCode(@Param("currencyCode") String currencyCode);

    /**
     * Deletes exchange rates older than the specified date.
     * <p>
     * Useful for cleaning up historical data and managing database size.
     *
     * @param date The cutoff date before which rates will be deleted
     * @return Number of ExchangeRate records deleted
     */
    long deleteByRateDateBefore(LocalDate date);
}
