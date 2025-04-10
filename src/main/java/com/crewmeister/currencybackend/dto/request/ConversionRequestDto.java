package com.crewmeister.currencybackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for currency conversion operations")
public class ConversionRequestDto {
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @Schema(description = "Amount to convert", example = "100.50", required = true)
    private BigDecimal amount;

    @NotNull(message = "From currency is required")
    @Schema(description = "Source currency code (ISO-4217)", example = "USD", required = true)
    private String fromCurrency;

    @NotNull(message = "Date is required")
    @Schema(description = "Date for the exchange rate (ISO-8601)", example = "2023-04-15", required = true)
    private LocalDate date;
}
