package com.adolcc.prices.application;

import java.time.LocalDateTime;

public record PriceQuery(long brandId, long productId, LocalDateTime instant) {
}
