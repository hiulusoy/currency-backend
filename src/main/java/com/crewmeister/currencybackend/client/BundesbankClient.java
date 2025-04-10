package com.crewmeister.currencybackend.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign Client for interacting with the Bundesbank API.
 * <p>
 * Provides methods to retrieve exchange rate data and metadata using declarative REST client.
 * <p>
 * Key Features:
 * - Retrieve all or specific exchange rate data
 * - Filter exchange rates by date range
 * - Fetch dataflow metadata
 * <p>
 * Configured with base URL from application properties
 *
 * @author hiulusoy
 */
@FeignClient(name = "bundesbank", url = "${bundesbank.api.url}")
public interface BundesbankClient {

    /**
     * Retrieves all available exchange rate data for a specific dataflow.
     * <p>
     * Fetches comprehensive exchange rate information in JSON format.
     *
     * @param flowRef Dataflow reference (typically 'BBEX3' for exchange rates)
     * @param format  Response format, defaulting to JSON
     * @param lang    Language for descriptions, defaulting to English
     * @return Raw JSON string containing all exchange rate data
     */
    @CircuitBreaker(name = "bundesbank")
    @Retry(name = "bundesbank")
    @RateLimiter(name = "bundesbank")
    @GetMapping(value = "/data/{flowRef}", produces = MediaType.APPLICATION_JSON_VALUE)
    String getAllData(@PathVariable("flowRef") String flowRef, @RequestParam(value = "format", defaultValue = "json") String format, @RequestParam(value = "lang", defaultValue = "en") String lang);

    /**
     * Retrieves specific exchange rate data for a given currency key.
     * <p>
     * Fetches exchange rate information for a specific currency in JSON format.
     *
     * @param flowRef Dataflow reference (typically 'BBEX3' for exchange rates)
     * @param key     Specific currency key (e.g., 'D.USD.EUR.BB.AC.000' for USD to EUR daily rate)
     * @param format  Response format, defaulting to JSON
     * @param lang    Language for descriptions, defaulting to English
     * @return Raw JSON string containing exchange rate data for the specified currency
     */
    @CircuitBreaker(name = "bundesbank")
    @Retry(name = "bundesbank")
    @RateLimiter(name = "bundesbank")
    @GetMapping(value = "/data/{flowRef}/{key}", produces = MediaType.APPLICATION_JSON_VALUE)
    String getData(@PathVariable("flowRef") String flowRef, @PathVariable("key") String key, @RequestParam(value = "format", defaultValue = "json") String format, @RequestParam(value = "lang", defaultValue = "en") String lang);

    /**
     * Retrieves exchange rate data for a specific currency within a date range.
     * <p>
     * Allows filtering of exchange rates by start and end dates in a specific format.
     *
     * @param flowRef     Dataflow reference (typically 'BBEX3' for exchange rates)
     * @param key         Specific currency key (e.g., 'D.USD.EUR.BB.AC.000' for USD to EUR daily rate)
     * @param format      Response format, defaulting to JSON
     * @param lang        Language for descriptions, defaulting to English
     * @param startPeriod Start date for filtering (yyyy-MM-dd format)
     * @param endPeriod   End date for filtering (yyyy-MM-dd format)
     * @return Raw JSON string containing exchange rate data for the specified currency and date range
     */
    @CircuitBreaker(name = "bundesbank")
    @Retry(name = "bundesbank")
    @RateLimiter(name = "bundesbank")
    @GetMapping(value = "/data/{flowRef}/{key}", produces = MediaType.APPLICATION_JSON_VALUE)
    String getDataForDateRange(@PathVariable("flowRef") String flowRef, @PathVariable("key") String key, @RequestParam(value = "format", defaultValue = "json") String format, @RequestParam(value = "lang", defaultValue = "en") String lang, @RequestParam(value = "startPeriod") String startPeriod, @RequestParam(value = "endPeriod") String endPeriod);

    /**
     * Retrieves metadata for a specific dataflow.
     * <p>
     * Fetches structural information about the dataflow in XML format.
     *
     * @param resourceID Metadata resource ID (typically 'BBEX3' for exchange rates)
     * @param format     Response format, defaulting to structured XML
     * @param lang       Language for descriptions, defaulting to English
     * @param references Reference inclusion level, defaulting to 'all'
     * @return XML string containing dataflow metadata
     */
    @CircuitBreaker(name = "bundesbank")
    @Retry(name = "bundesbank")
    @RateLimiter(name = "bundesbank")
    @GetMapping(value = "/metadata/dataflow/BBK/{resourceID}")
    String getDataflow(@PathVariable("resourceID") String resourceID, @RequestParam(value = "format", defaultValue = "struct_xml") String format, @RequestParam(value = "lang", defaultValue = "en") String lang, @RequestParam(value = "references", defaultValue = "all") String references);
}
