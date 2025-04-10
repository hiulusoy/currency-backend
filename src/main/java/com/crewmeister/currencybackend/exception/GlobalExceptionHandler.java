package com.crewmeister.currencybackend.exception;

import com.crewmeister.currencybackend.dto.wrapper.ExceptionWrapper;
import com.crewmeister.currencybackend.dto.wrapper.ValidationExceptionWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({Exception.class, RuntimeException.class, Throwable.class})
    public ResponseEntity<ExceptionWrapper> handleGenericExceptions(Throwable exception) {
        log.error(exception.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ExceptionWrapper.builder()
                        .success(false)
                        .message("Action failed: An error occurred!")
                        .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .localDateTime(LocalDateTime.now())
                        .build());
    }


    @ExceptionHandler({CurrencyNotFoundException.class, ExchangeRateNotFoundException.class})
    public ResponseEntity<ExceptionWrapper> handleNotFoundsException(Throwable exception) {
        log.error(exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ExceptionWrapper.builder()
                        .success(false)
                        .message("Action failed: " + exception.getMessage())
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .localDateTime(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ExceptionWrapper> handleExternalServiceException(Throwable exception) {
        log.error(exception.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ExceptionWrapper.builder()
                        .success(false)
                        .message("Action failed: " + exception.getMessage())
                        .httpStatus(HttpStatus.SERVICE_UNAVAILABLE)
                        .localDateTime(LocalDateTime.now())
                        .build());
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionWrapper> handleValidationExceptions(MethodArgumentNotValidException exception) {
        log.error(exception.getMessage());
        ExceptionWrapper exceptionWrapper = ExceptionWrapper.builder()
                .success(false)
                .message("Invalid Input(s): Validation error!")
                .httpStatus(HttpStatus.BAD_REQUEST)
                .localDateTime(LocalDateTime.now())
                .build();

        List<ValidationExceptionWrapper> validationExceptions = collectValidationExceptions(exception);

        exceptionWrapper.setValidationExceptions(validationExceptions);
        exceptionWrapper.setErrorCount(validationExceptions.size());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionWrapper);

    }

    private List<ValidationExceptionWrapper> collectValidationExceptions(MethodArgumentNotValidException exception) {

        List<ValidationExceptionWrapper> validationExceptions = new ArrayList<>();

        for (ObjectError error : exception.getBindingResult().getAllErrors()) {

            String fieldName = ((FieldError) error).getField();
            Object rejectedValue = ((FieldError) error).getRejectedValue();
            String errorMessage = error.getDefaultMessage();

            ValidationExceptionWrapper validationException = ValidationExceptionWrapper.builder()
                    .errorField(fieldName)
                    .rejectedValue(rejectedValue)
                    .reason(errorMessage)
                    .build();

            validationExceptions.add(validationException);

        }

        return validationExceptions;

    }

}

