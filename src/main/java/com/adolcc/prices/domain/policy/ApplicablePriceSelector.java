package com.adolcc.prices.domain.policy;

import com.adolcc.prices.domain.exception.PriceNotFoundException;
import com.adolcc.prices.domain.model.Price;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public class ApplicablePriceSelector {

    public Price select(List<Price> prices, LocalDateTime instant) {
        return prices.stream()
                .filter(price -> price.validity().contains(instant))
                .max(Comparator.comparingInt(Price::priority))
                .orElseThrow(PriceNotFoundException::new);
    }
}
