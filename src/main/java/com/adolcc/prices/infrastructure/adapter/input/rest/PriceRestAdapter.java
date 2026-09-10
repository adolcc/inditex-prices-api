package com.adolcc.prices.infrastructure.adapter.input.rest;

import com.adolcc.prices.application.PriceQuery;
import com.adolcc.prices.application.port.in.FindApplicablePriceUseCase;
import com.adolcc.prices.domain.model.Price;
import com.adolcc.prices.infrastructure.adapter.input.rest.contract.PricesApi;
import com.adolcc.prices.infrastructure.adapter.input.rest.contract.dto.PriceResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public class PriceRestAdapter implements PricesApi {

    private final FindApplicablePriceUseCase findApplicablePriceUseCase;

    public PriceRestAdapter(FindApplicablePriceUseCase findApplicablePriceUseCase) {
        this.findApplicablePriceUseCase = findApplicablePriceUseCase;
    }

    @Override
    public ResponseEntity<PriceResponse> findApplicablePrice(LocalDateTime applicationDate, Long productId,
            Integer brandId) {
        Price applicablePrice = findApplicablePriceUseCase.findApplicablePrice(
                new PriceQuery(brandId, productId, applicationDate));

        return ResponseEntity.ok(PriceRestMapper.toResponse(applicablePrice));
    }
}
