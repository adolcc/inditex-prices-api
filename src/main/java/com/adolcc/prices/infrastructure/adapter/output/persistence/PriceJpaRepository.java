package com.adolcc.prices.infrastructure.adapter.output.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PriceJpaRepository extends JpaRepository<PriceJpaEntity, Long> {

    List<PriceJpaEntity> findByBrandIdAndProductId(long brandId, long productId);
}
