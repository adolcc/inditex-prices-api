package com.adolcc.prices.application;

import com.adolcc.prices.application.port.out.PriceRepository;
import com.adolcc.prices.domain.model.Price;

import java.util.List;

class InMemoryPriceRepository implements PriceRepository {

    private final List<Price> prices;

    InMemoryPriceRepository(List<Price> prices) {
        this.prices = prices;
    }

    @Override
    public List<Price> findByBrandAndProduct(long brandId, long productId) {
        return prices.stream()
                .filter(price -> price.brandId() == brandId)
                .filter(price -> price.productId() == productId)
                .toList();
    }
}
