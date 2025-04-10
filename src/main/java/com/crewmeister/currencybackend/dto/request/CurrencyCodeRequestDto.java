package com.crewmeister.currencybackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object containing a currency code")
public class CurrencyCodeRequestDto {

    @NotNull(message = "Currency code cannot be null")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency code must be 3 uppercase letters")
    @Schema(
            description = "ISO-4217 currency code",
            example = "USD",
            required = true,
            pattern = "^[A-Z]{3}$",
            minLength = 3,
            maxLength = 3
    )
    private String code;
}
