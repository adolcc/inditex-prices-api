package com.adolcc.prices.domain.model;

import java.math.BigDecimal;

public record Price(
        long brandId,
        long productId,
        int priceList,
        int priority,
        BigDecimal amount,
        String currency,
        DateRange validity) {
}
