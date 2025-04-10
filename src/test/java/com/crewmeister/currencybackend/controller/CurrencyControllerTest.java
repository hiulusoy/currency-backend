package com.crewmeister.currencybackend.controller;

import com.crewmeister.currencybackend.dto.CurrencyDto;
import com.crewmeister.currencybackend.dto.request.CurrencyCodeRequestDto;
import com.crewmeister.currencybackend.service.CurrencyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CurrencyController.class)
class CurrencyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CurrencyService currencyService;

    @Autowired
    private ObjectMapper objectMapper;

    private List<CurrencyDto> currencies;
    private CurrencyDto usdCurrency;

    @BeforeEach
    void setUp() {
        // Setup test currency data
        usdCurrency = CurrencyDto.builder()
                .code("USD")
                .name("US Dollar")
                .country("United States")
                .active(true)
                .build();

        CurrencyDto eurCurrency = CurrencyDto.builder()
                .code("EUR")
                .name("Euro")
                .country("European Union")
                .active(true)
                .build();

        CurrencyDto gbpCurrency = CurrencyDto.builder()
                .code("GBP")
                .name("British Pound")
                .country("United Kingdom")
                .active(true)
                .build();

        CurrencyDto inrCurrency = CurrencyDto.builder()
                .code("INR")
                .name("Indian Rupee")
                .country("India")
                .active(false)
                .build();

        currencies = Arrays.asList(usdCurrency, eurCurrency, gbpCurrency, inrCurrency);
    }

    @Test
    @DisplayName("Should return all currencies")
    void shouldReturnAllCurrencies() throws Exception {
        // Given
        given(currencyService.getAllCurrencies()).willReturn(currencies);

        // When & Then
        mockMvc.perform(get("/api/v1/currencies")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].code", is("USD")))
                .andExpect(jsonPath("$[1].code", is("EUR")))
                .andExpect(jsonPath("$[2].code", is("GBP")))
                .andExpect(jsonPath("$[3].code", is("INR")));

        verify(currencyService).getAllCurrencies();
    }

    @Test
    @DisplayName("Should return only active currencies")
    void shouldReturnActiveCurrencies() throws Exception {
        // Given
        List<CurrencyDto> activeCurrencies = Arrays.asList(usdCurrency, 
                currencies.get(1), currencies.get(2)); // USD, EUR, GBP
        
        given(currencyService.getActiveCurrencies()).willReturn(activeCurrencies);

        // When & Then
        mockMvc.perform(get("/api/v1/currencies/active")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].code", is("USD")))
                .andExpect(jsonPath("$[0].active", is(true)))
                .andExpect(jsonPath("$[1].code", is("EUR")))
                .andExpect(jsonPath("$[1].active", is(true)))
                .andExpect(jsonPath("$[2].code", is("GBP")))
                .andExpect(jsonPath("$[2].active", is(true)));

        verify(currencyService).getActiveCurrencies();
    }

    @Test
    @DisplayName("Should return a currency by its code")
    void shouldReturnCurrencyByCode() throws Exception {
        // Given
        String currencyCode = "USD";
        CurrencyCodeRequestDto request = new CurrencyCodeRequestDto();
        request.setCode(currencyCode);
        
        given(currencyService.getCurrencyByCode(currencyCode)).willReturn(usdCurrency);

        // When & Then
        mockMvc.perform(post("/api/v1/currencies/by-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(currencyCode)))
                .andExpect(jsonPath("$.name", is("US Dollar")))
                .andExpect(jsonPath("$.country", is("United States")))
                .andExpect(jsonPath("$.active", is(true)));

        verify(currencyService).getCurrencyByCode(currencyCode);
    }

    @Test
    @DisplayName("Should validate currency code request")
    void shouldValidateCurrencyCodeRequest() throws Exception {
        // Given
        CurrencyCodeRequestDto invalidRequest = new CurrencyCodeRequestDto();
        // code is null, which should trigger validation error

        // When & Then
        mockMvc.perform(post("/api/v1/currencies/by-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return currencies by country")
    void shouldReturnCurrenciesByCountry() throws Exception {
        // Given
        String country = "United States";
        List<CurrencyDto> usCurrencies = Arrays.asList(usdCurrency);
        
        given(currencyService.getCurrenciesByCountry(country)).willReturn(usCurrencies);

        // When & Then
        mockMvc.perform(get("/api/v1/currencies/country/{country}", country)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is("USD")))
                .andExpect(jsonPath("$[0].country", is(country)));

        verify(currencyService).getCurrenciesByCountry(country);
    }

    @Test
    @DisplayName("Should return empty list when no currencies found for country")
    void shouldReturnEmptyListWhenNoCurrenciesFoundForCountry() throws Exception {
        // Given
        String country = "Unknown Country";
        List<CurrencyDto> emptyCurrencies = Arrays.asList();
        
        given(currencyService.getCurrenciesByCountry(country)).willReturn(emptyCurrencies);

        // When & Then
        mockMvc.perform(get("/api/v1/currencies/country/{country}", country)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(currencyService).getCurrenciesByCountry(country);
    }
}
