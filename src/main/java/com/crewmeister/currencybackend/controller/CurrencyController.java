package com.crewmeister.currencybackend.controller;

import com.crewmeister.currencybackend.dto.CurrencyDto;
import com.crewmeister.currencybackend.dto.request.CurrencyCodeRequestDto;
import com.crewmeister.currencybackend.service.CurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * REST Controller for managing currency-related operations.
 * <p>
 * Provides endpoints for retrieving currency information through various methods:
 * - Fetching all currencies
 * - Retrieving active currencies
 * - Getting a specific currency by its code
 * - Finding currencies by country
 * <p>
 * Mapped to the base endpoint: /api/v1/currencies
 *
 * @author hiulusoy
 */
@RestController
@RequestMapping("/api/v1/currencies")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Currencies", description = "Currency management API with functionality for retrieving currency information")
public class CurrencyController {

    /**
     * Service layer for currency-related business logic
     */
    private final CurrencyService currencyService;

    /**
     * Retrieves all currencies in the system.
     * <p>
     * HTTP Method: GET
     * Endpoint: /api/v1/currencies
     *
     * @return ResponseEntity containing a list of all CurrencyDto objects
     * Returns HTTP 200 (OK) with the list of currencies
     */
    @GetMapping
    @Operation(summary = "Get all currencies", description = "Retrieves a list of all currencies available in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all currencies",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CurrencyDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<CurrencyDto>> getAllCurrencies() {
        log.info("Request to get all currencies");
        List<CurrencyDto> currencies = currencyService.getAllCurrencies();
        return ResponseEntity.ok(currencies);
    }

    /**
     * Retrieves only active currencies in the system.
     * <p>
     * HTTP Method: GET
     * Endpoint: /api/v1/currencies/active
     *
     * @return ResponseEntity containing a list of active CurrencyDto objects
     * Returns HTTP 200 (OK) with the list of active currencies
     */
    @GetMapping("/active")
    @Operation(summary = "Get active currencies", description = "Retrieves a list of only active currencies in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved active currencies",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CurrencyDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<CurrencyDto>> getActiveCurrencies() {
        log.info("Request to get active currencies");
        List<CurrencyDto> currencies = currencyService.getActiveCurrencies();
        return ResponseEntity.ok(currencies);
    }

    /**
     * Retrieves a specific currency by its unique code.
     * <p>
     * HTTP Method: POST
     * Endpoint: /api/v1/currencies/by-code
     *
     * @param request CurrencyCodeRequestDto containing the currency code
     *                Validated using Bean Validation
     * @return ResponseEntity containing the CurrencyDto for the specified code
     * Returns HTTP 200 (OK) with the currency details
     */
    @PostMapping("/by-code")
    @Operation(summary = "Get currency by code", description = "Retrieves a specific currency by its unique currency code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the currency",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CurrencyDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid currency code format"),
            @ApiResponse(responseCode = "404", description = "Currency not found with the given code"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<CurrencyDto> getCurrencyByCode(
            @Parameter(description = "Currency code request object", required = true)
            @RequestBody @Valid CurrencyCodeRequestDto request) {
        log.info("Request to get currency with code: {}", request.getCode());
        CurrencyDto currency = currencyService.getCurrencyByCode(request.getCode());
        return ResponseEntity.ok(currency);
    }

    /**
     * Retrieves currencies associated with a specific country.
     * <p>
     * HTTP Method: GET
     * Endpoint: /api/v1/currencies/country/{country}
     *
     * @param country The name of the country to retrieve currencies for
     * @return ResponseEntity containing a list of CurrencyDto objects for the country
     * Returns HTTP 200 (OK) with the list of currencies
     */
    @GetMapping("/country/{country}")
    @Operation(summary = "Get currencies by country", description = "Retrieves all currencies associated with a specific country")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved currencies for the country",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CurrencyDto.class))),
            @ApiResponse(responseCode = "404", description = "No currencies found for the country"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<CurrencyDto>> getCurrenciesByCountry(
            @Parameter(description = "Country name", required = true, example = "United States")
            @PathVariable String country) {
        log.info("Request to get currencies for country: {}", country);
        List<CurrencyDto> currencies = currencyService.getCurrenciesByCountry(country);
        return ResponseEntity.ok(currencies);
    }
}
