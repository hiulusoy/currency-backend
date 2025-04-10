package com.crewmeister.currencybackend.dto.wrapper;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ValidationExceptionWrapper {

    private String errorField;
    private Object rejectedValue;
    private String reason;

}
