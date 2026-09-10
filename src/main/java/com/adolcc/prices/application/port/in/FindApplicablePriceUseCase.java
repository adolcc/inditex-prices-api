package com.adolcc.prices.application.port.in;

import com.adolcc.prices.application.PriceQuery;
import com.adolcc.prices.domain.model.Price;

public interface FindApplicablePriceUseCase {

    Price findApplicablePrice(PriceQuery query);
}
