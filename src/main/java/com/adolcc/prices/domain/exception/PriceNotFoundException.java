package com.adolcc.prices.domain.exception;

public class PriceNotFoundException extends RuntimeException {

    public PriceNotFoundException() {
        super("No existe ninguna tarifa aplicable para los parámetros indicados");
    }
}
