package com.adolcc.prices.infrastructure.adapter.input.rest;

import com.adolcc.prices.domain.model.Price;
import com.adolcc.prices.infrastructure.adapter.input.rest.contract.dto.PriceResponse;

final class PriceRestMapper {

    private PriceRestMapper() {
    }

    static PriceResponse toResponse(Price price) {
        return new PriceResponse(
                price.productId(),
                (int) price.brandId(),
                price.priceList(),
                price.validity().startsAt(),
                price.validity().endsAt(),
                price.amount(),
                price.currency());
    }
}
