package com.adolcc.prices.infrastructure.config;

import com.adolcc.prices.application.FindApplicablePriceService;
import com.adolcc.prices.application.port.in.FindApplicablePriceUseCase;
import com.adolcc.prices.application.port.out.PriceRepository;
import com.adolcc.prices.domain.policy.ApplicablePriceSelector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PriceConfiguration {

    @Bean
    public FindApplicablePriceUseCase findApplicablePriceUseCase(PriceRepository priceRepository) {
        return new FindApplicablePriceService(priceRepository, new ApplicablePriceSelector());
    }
}
