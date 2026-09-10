package com.adolcc.prices.application;

import com.adolcc.prices.application.port.in.FindApplicablePriceUseCase;
import com.adolcc.prices.application.port.out.PriceRepository;
import com.adolcc.prices.domain.model.Price;
import com.adolcc.prices.domain.policy.ApplicablePriceSelector;

public class FindApplicablePriceService implements FindApplicablePriceUseCase {

    private final PriceRepository priceRepository;
    private final ApplicablePriceSelector applicablePriceSelector;

    public FindApplicablePriceService(PriceRepository priceRepository,
            ApplicablePriceSelector applicablePriceSelector) {
        this.priceRepository = priceRepository;
        this.applicablePriceSelector = applicablePriceSelector;
    }

    @Override
    public Price findApplicablePrice(PriceQuery query) {
        return applicablePriceSelector.select(
                priceRepository.findByBrandAndProduct(query.brandId(), query.productId()),
                query.instant());
    }
}
