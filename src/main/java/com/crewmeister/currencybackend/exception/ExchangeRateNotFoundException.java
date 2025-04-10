package com.crewmeister.currencybackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDate;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ExchangeRateNotFoundException extends RuntimeException {

    public ExchangeRateNotFoundException(String currencyCode, LocalDate date) {
        super("Exchange rate not found for currency: " + currencyCode + " and date: " + date);
    }
}
