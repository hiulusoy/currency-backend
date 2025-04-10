package com.crewmeister.currencybackend.repository;

import com.crewmeister.currencybackend.entity.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for performing database operations on Currency entities.
 * Key Features:
 * - Find active currencies
 * - Retrieve currency by its unique code
 * - Find currencies by country
 *
 * @author hiulusoy
 */
@Repository
public interface CurrencyRepository extends JpaRepository<Currency, String> {

    /**
     * Retrieves all active currencies from the database.
     * <p>
     * An active currency is determined by the 'active' flag set to true
     * in the Currency entity.
     *
     * @return A list of active Currency entities
     */
    List<Currency> findByActiveTrue();

    /**
     * Finds a specific currency by its unique currency code.
     *
     * @param code The unique identifier of the currency (e.g., "USD", "EUR")
     * @return An Optional containing the Currency if found, or an empty Optional
     */
    Optional<Currency> findByCode(String code);

    /**
     * Retrieves all currencies associated with a specific country.
     *
     * @param country The name of the country
     * @return A list of Currency entities for the specified country
     */
    List<Currency> findByCountry(String country);
}
