// CurrencyMapper.java
package com.crewmeister.currencybackend.mapper;

import com.crewmeister.currencybackend.dto.CurrencyDto;
import com.crewmeister.currencybackend.entity.Currency;
import org.springframework.stereotype.Component;

@Component
public class CurrencyMapper {


    /**
     * Map Currency entity to CurrencyDto
     */
    public CurrencyDto mapToDto(Currency currency) {
        return CurrencyDto.builder()
                .code(currency.getCode())
                .name(currency.getName())
                .country(currency.getCountry())
                .active(currency.isActive())
                .build();
    }
}
