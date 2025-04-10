package com.crewmeister.currencybackend.controller;

import com.crewmeister.currencybackend.annotation.ExecutionTime;
import com.crewmeister.currencybackend.dto.ExchangeRateDto;
import com.crewmeister.currencybackend.dto.request.ConversionRequestDto;
import com.crewmeister.currencybackend.dto.request.RatesByDateRequestDto;
import com.crewmeister.currencybackend.dto.response.ConversionResponseDto;
import com.crewmeister.currencybackend.service.ExchangeRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for managing exchange rate operations.
 * <p>
 * Provides comprehensive endpoints for:
 * - Retrieving exchange rates
 * - Fetching rates by date and currency
 * - Currency conversion to EUR
 * <p>
 * Mapped to the base endpoint: /api/v1/exchange-rates
 *
 * @author hiulusoy
 */
@RestController
@RequestMapping("/api/v1/exchange-rates")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Exchange Rates", description = "Exchange Rate API with conversion and historical data capabilities")
public class ExchangeRateController {

    /**
     * Service layer for exchange rate-related business logic
     */
    private final ExchangeRateService exchangeRateService;

    /**
     * Retrieves all current exchange rates.
     * <p>
     * HTTP Method: GET
     * Endpoint: /api/v1/exchange-rates
     *
     * @return ResponseEntity containing a list of all ExchangeRateDto objects
     * Returns HTTP 200 (OK) with the list of exchange rates
     */
    @ExecutionTime
    @GetMapping
    @Operation(summary = "Get all current exchange rates", description = "Retrieves a list of all available exchange rates")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved exchange rates",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExchangeRateDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<ExchangeRateDto>> getAllRates() {
        log.info("Request to get all exchange rates");
        List<ExchangeRateDto> rates = exchangeRateService.getAllRates();
        return ResponseEntity.ok(rates);
    }

    /**
     * Retrieves exchange rates for a specific date.
     * <p>
     * HTTP Method: POST
     * Endpoint: /api/v1/exchange-rates/date
     *
     * @param request RatesByDateRequestDto containing the date to fetch rates for
     *                Validated using Bean Validation
     * @return ResponseEntity containing a list of ExchangeRateDto for the specified date
     * Returns HTTP 200 (OK) with the list of exchange rates
     */
    @ExecutionTime
    @PostMapping("/date")
    @Operation(summary = "Get exchange rates by date",
            description = "Retrieves all exchange rates for a specific date")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved exchange rates for the date",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExchangeRateDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid date format"),
            @ApiResponse(responseCode = "404", description = "No exchange rates found for the date"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<ExchangeRateDto>> getRatesByDate(
            @Parameter(description = "Date request object", required = true)
            @RequestBody @Valid RatesByDateRequestDto request) {
        log.info("Request to get exchange rates for date: {}", request.getDate());
        List<ExchangeRateDto> rates = exchangeRateService.getRatesByDate(request.getDate());
        return ResponseEntity.ok(rates);
    }

    /**
     * Retrieves the exchange rate for a specific currency on a given date.
     * <p>
     * HTTP Method: GET
     * Endpoint: /api/v1/exchange-rates/{currencyCode}/date/{date}
     *
     * @param currencyCode The currency code to retrieve the rate for
     * @param date         The specific date for the exchange rate
     * @return ResponseEntity containing the ExchangeRateDto for the specified currency and date
     * Returns HTTP 200 (OK) with the exchange rate
     */
    @ExecutionTime
    @GetMapping("/{currencyCode}/date/{date}")
    @Operation(summary = "Get exchange rate by currency and date",
            description = "Retrieves the exchange rate for a specific currency on a given date")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved exchange rate",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExchangeRateDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid currency code or date format"),
            @ApiResponse(responseCode = "404", description = "Exchange rate not found for the currency and date"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ExchangeRateDto> getRateByCurrencyAndDate(
            @Parameter(description = "Currency code (e.g., USD, GBP)", required = true)
            @PathVariable String currencyCode,
            @Parameter(description = "Date in ISO format (YYYY-MM-DD)", required = true, example = "2023-04-15")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("Request to get exchange rate for currency: {} and date: {}", currencyCode, date);
        ExchangeRateDto rate = exchangeRateService.getRateByCurrencyAndDate(currencyCode, date);
        return ResponseEntity.ok(rate);
    }

    /**
     * Converts an amount from a specified currency to EUR.
     * <p>
     * HTTP Method: POST
     * Endpoint: /api/v1/exchange-rates/convert
     *
     * @param request ConversionRequestDto containing conversion details
     * @return ResponseEntity containing the ConversionResponseDto with converted amount
     * Returns HTTP 200 (OK) with the conversion result
     */
    @ExecutionTime
    @PostMapping("/convert")
    @Operation(summary = "Convert currency to EUR",
            description = "Converts an amount from specified currency to EUR using the exchange rate for the given date")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully converted the amount",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ConversionResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "404", description = "Exchange rate not found for the currency and date"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ConversionResponseDto> convertToEur(
            @Parameter(description = "Conversion request details", required = true)
            @RequestBody @Valid ConversionRequestDto request) {
        log.info("Request to convert {} {} to EUR on date: {}",
                request.getAmount(), request.getFromCurrency(), request.getDate());
        ConversionResponseDto result = exchangeRateService.convertToEur(request);
        return ResponseEntity.ok(result);
    }

    /**
     * Provides a quick conversion endpoint using GET method with path variables.
     * <p>
     * Useful for scenarios requiring direct URL-based currency conversion.
     * <p>
     * HTTP Method: GET
     * Endpoint: /api/v1/exchange-rates/convert/{currencyCode}/{amount}/date/{date}
     *
     * @param currencyCode The source currency code
     * @param amount       The amount to convert
     * @param date         The date for the conversion rate
     * @return ResponseEntity containing the ConversionResponseDto with converted amount
     * Returns HTTP 200 (OK) with the conversion result
     */
    @ExecutionTime
    @GetMapping("/convert/{currencyCode}/{amount}/date/{date}")
    @Operation(summary = "Quick convert currency to EUR",
            description = "Simple GET endpoint to convert currency to EUR using path variables")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully converted the amount",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ConversionResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid currency code, amount, or date format"),
            @ApiResponse(responseCode = "404", description = "Exchange rate not found for the currency and date"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ConversionResponseDto> quickConvert(
            @Parameter(description = "Currency code (e.g., USD, GBP)", required = true)
            @PathVariable String currencyCode,
            @Parameter(description = "Amount to convert", required = true, example = "100.50")
            @PathVariable BigDecimal amount,
            @Parameter(description = "Date in ISO format (YYYY-MM-DD)", required = true, example = "2023-04-15")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("Quick convert request for {} {} on date: {}", amount, currencyCode, date);

        ConversionRequestDto request = ConversionRequestDto.builder()
                .fromCurrency(currencyCode)
                .amount(amount)
                .date(date)
                .build();

        ConversionResponseDto result = exchangeRateService.convertToEur(request);
        return ResponseEntity.ok(result);
    }
}
