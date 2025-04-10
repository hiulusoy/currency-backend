package com.crewmeister.currencybackend.controller;

import com.crewmeister.currencybackend.dto.ExchangeRateDto;
import com.crewmeister.currencybackend.dto.request.ConversionRequestDto;
import com.crewmeister.currencybackend.dto.request.RatesByDateRequestDto;
import com.crewmeister.currencybackend.dto.response.ConversionResponseDto;
import com.crewmeister.currencybackend.service.ExchangeRateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExchangeRateController.class)
class ExchangeRateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExchangeRateService exchangeRateService;

    @Autowired
    private ObjectMapper objectMapper;

    private List<ExchangeRateDto> exchangeRates;
    private ExchangeRateDto singleRate;
    private ConversionResponseDto conversionResponse;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        testDate = LocalDate.of(2025, 4, 9);

        // Setup test data - exchange rates
        exchangeRates = Arrays.asList(
                ExchangeRateDto.builder()
                        .currencyCode("USD")
                        .currencyName("US Dollar")
                        .date(testDate)
                        .rate(new BigDecimal("0.92"))
                        .build(),
                ExchangeRateDto.builder()
                        .currencyCode("GBP")
                        .currencyName("British Pound")
                        .date(testDate)
                        .rate(new BigDecimal("1.17"))
                        .build()
        );

        // Setup single rate for testing specific currency endpoint
        singleRate = ExchangeRateDto.builder()
                .currencyCode("USD")
                .currencyName("US Dollar")
                .date(testDate)
                .rate(new BigDecimal("0.92"))
                .build();

        // Setup conversion response
        conversionResponse = ConversionResponseDto.builder()
                .amount(new BigDecimal("100"))
                .fromCurrency("USD")
                .convertedAmount(new BigDecimal("92"))
                .toCurrency("EUR")
                .exchangeRate(new BigDecimal("0.92"))
                .date(testDate)
                .build();
    }

    @Test
    @DisplayName("Should return all exchange rates")
    void shouldReturnAllExchangeRates() throws Exception {
        // Given
        given(exchangeRateService.getAllRates()).willReturn(exchangeRates);

        // When & Then
        mockMvc.perform(get("/api/v1/exchange-rates")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].currencyCode", is("USD")))
                .andExpect(jsonPath("$[0].rate", is(0.92)))
                .andExpect(jsonPath("$[1].currencyCode", is("GBP")))
                .andExpect(jsonPath("$[1].rate", is(1.17)));

        verify(exchangeRateService).getAllRates();
    }

    @Test
    @DisplayName("Should return exchange rates for a specific date")
    void shouldReturnExchangeRatesByDate() throws Exception {
        // Given
        RatesByDateRequestDto request = new RatesByDateRequestDto();
        request.setDate(testDate);

        given(exchangeRateService.getRatesByDate(testDate)).willReturn(exchangeRates);

        // When & Then
        mockMvc.perform(post("/api/v1/exchange-rates/date")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].currencyCode", is("USD")))
                .andExpect(jsonPath("$[0].date", is(testDate.toString())))
                .andExpect(jsonPath("$[1].currencyCode", is("GBP")));

        verify(exchangeRateService).getRatesByDate(testDate);
    }

    @Test
    @DisplayName("Should return exchange rate for a specific currency and date")
    void shouldReturnRateByCurrencyAndDate() throws Exception {
        // Given
        String currencyCode = "USD";

        given(exchangeRateService.getRateByCurrencyAndDate(currencyCode, testDate))
                .willReturn(singleRate);

        // When & Then
        mockMvc.perform(get("/api/v1/exchange-rates/{currencyCode}/date/{date}", currencyCode, testDate)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currencyCode", is(currencyCode)))
                .andExpect(jsonPath("$.date", is(testDate.toString())))
                .andExpect(jsonPath("$.rate", is(0.92)));

        verify(exchangeRateService).getRateByCurrencyAndDate(currencyCode, testDate);
    }

    @Test
    @DisplayName("Should convert currency amount to EUR via POST endpoint")
    void shouldConvertCurrencyToEurPost() throws Exception {
        // Given
        ConversionRequestDto request = ConversionRequestDto.builder()
                .fromCurrency("USD")
                .amount(new BigDecimal("100"))
                .date(testDate)
                .build();

        given(exchangeRateService.convertToEur(any(ConversionRequestDto.class)))
                .willReturn(conversionResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/exchange-rates/convert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount", is(100)))
                .andExpect(jsonPath("$.fromCurrency", is("USD")))
                .andExpect(jsonPath("$.convertedAmount", is(92)))
                .andExpect(jsonPath("$.toCurrency", is("EUR")))
                .andExpect(jsonPath("$.exchangeRate", is(0.92)))
                .andExpect(jsonPath("$.date", is(testDate.toString())));

        verify(exchangeRateService).convertToEur(any(ConversionRequestDto.class));
    }

    @Test
    @DisplayName("Should convert currency amount to EUR via GET endpoint")
    void shouldConvertCurrencyToEurGet() throws Exception {
        // Given
        String currencyCode = "USD";
        BigDecimal amount = new BigDecimal("100");

        given(exchangeRateService.convertToEur(any(ConversionRequestDto.class)))
                .willReturn(conversionResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/exchange-rates/convert/{currencyCode}/{amount}/date/{date}",
                        currencyCode, amount, testDate)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount", is(100)))
                .andExpect(jsonPath("$.fromCurrency", is("USD")))
                .andExpect(jsonPath("$.convertedAmount", is(92)))
                .andExpect(jsonPath("$.toCurrency", is("EUR")))
                .andExpect(jsonPath("$.exchangeRate", is(0.92)))
                .andExpect(jsonPath("$.date", is(testDate.toString())));

        verify(exchangeRateService).convertToEur(any(ConversionRequestDto.class));
    }

    @Test
    @DisplayName("Should validate request body for rates by date")
    void shouldValidateRatesByDateRequest() throws Exception {
        // Given
        RatesByDateRequestDto invalidRequest = new RatesByDateRequestDto();
        // date is null, which should trigger validation error

        // When & Then
        mockMvc.perform(post("/api/v1/exchange-rates/date")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should validate request body for conversion")
    void shouldValidateConversionRequest() throws Exception {
        // Given
        ConversionRequestDto invalidRequest = ConversionRequestDto.builder()
                .fromCurrency("USD")
                // amount is null, which should trigger validation error
                .date(testDate)
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/exchange-rates/convert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should validate amount is positive for conversion request")
    void shouldValidateAmountIsPositive() throws Exception {
        // Given
        ConversionRequestDto invalidRequest = ConversionRequestDto.builder()
                .fromCurrency("USD")
                .amount(new BigDecimal("-100")) // negative amount should trigger validation error
                .date(testDate)
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/exchange-rates/convert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
