package com.crewmeister.currencybackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for retrieving an exchange rate by currency code and date")
public class RateByCurrencyAndDateRequestDto {

    @NotNull(message = "Currency code cannot be null")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency code must be 3 uppercase letters")
    @Schema(
            description = "currency code",
            example = "USD",
            required = true,
            pattern = "^[A-Z]{3}$",
            minLength = 3,
            maxLength = 3
    )
    private String currencyCode;

    @NotNull(message = "Date cannot be null")
    @Schema(
            description = "Date for retrieving the exchange rate",
            example = "2023-04-15",
            required = true,
            format = "date"
    )
    private LocalDate date;
}
