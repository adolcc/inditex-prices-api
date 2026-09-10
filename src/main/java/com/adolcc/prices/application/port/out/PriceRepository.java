package com.adolcc.prices.application.port.out;

import com.adolcc.prices.domain.model.Price;

import java.util.List;

public interface PriceRepository {

    List<Price> findByBrandAndProduct(long brandId, long productId);
}
