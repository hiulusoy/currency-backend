package com.crewmeister.currencybackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for fetching exchange rates by date")
public class RatesByDateRequestDto {

    @NotNull(message = "Date cannot be null")
    @Schema(
            description = "Date for retrieving exchange rates (ISO-8601)",
            example = "2023-04-15",
            required = true,
            format = "date"
    )
    private LocalDate date;
}
