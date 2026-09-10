package com.adolcc.prices.infrastructure.adapter.input.rest;

import com.adolcc.prices.domain.exception.PriceNotFoundException;
import com.adolcc.prices.infrastructure.adapter.input.rest.contract.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class RestExceptionHandler {

    private static final String PRICE_NOT_FOUND = "PRICE_NOT_FOUND";
    private static final String INVALID_PARAMETERS = "INVALID_PARAMETERS";
    private static final String INVALID_PARAMETERS_MESSAGE = "Alguno de los parámetros indicados falta o tiene un formato incorrecto";

    @ExceptionHandler(PriceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePriceNotFound(PriceNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(PRICE_NOT_FOUND, exception.getMessage()));
    }

    @ExceptionHandler({ MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class })
    public ResponseEntity<ErrorResponse> handleInvalidParameters() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(INVALID_PARAMETERS, INVALID_PARAMETERS_MESSAGE));
    }
}
