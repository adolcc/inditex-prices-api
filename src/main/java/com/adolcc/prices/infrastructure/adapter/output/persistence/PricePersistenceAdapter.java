package com.adolcc.prices.infrastructure.adapter.output.persistence;

import com.adolcc.prices.application.port.out.PriceRepository;
import com.adolcc.prices.domain.model.Price;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PricePersistenceAdapter implements PriceRepository {

    private final PriceJpaRepository priceJpaRepository;

    public PricePersistenceAdapter(PriceJpaRepository priceJpaRepository) {
        this.priceJpaRepository = priceJpaRepository;
    }

    @Override
    public List<Price> findByBrandAndProduct(long brandId, long productId) {
        return priceJpaRepository.findByBrandIdAndProductId(brandId, productId).stream()
                .map(PriceJpaEntity::toDomain)
                .toList();
    }
}
