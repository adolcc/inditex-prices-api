package com.adolcc.prices.application;

import com.adolcc.prices.domain.exception.PriceNotFoundException;
import com.adolcc.prices.domain.model.DateRange;
import com.adolcc.prices.domain.model.Price;
import com.adolcc.prices.domain.policy.ApplicablePriceSelector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class FindApplicablePriceServiceTest {

    private static final long BRAND_ID = 1L;
    private static final long OTHER_BRAND_ID = 2L;
    private static final long PRODUCT_ID = 35455L;
    private static final long OTHER_PRODUCT_ID = 99999L;
    private static final String CURRENCY = "EUR";
    private static final LocalDateTime INSTANT = LocalDateTime.of(2020, 6, 14, 10, 0);

    private static final PriceQuery QUERY = new PriceQuery(BRAND_ID, PRODUCT_ID, INSTANT);

    private static final Price LOWER_PRIORITY_COVERING_THE_INSTANT = price(BRAND_ID, PRODUCT_ID, 5, 0,
            "2020-06-14T00:00:00", "2020-12-31T23:59:59", "10.10");

    private static final Price HIGHER_PRIORITY_OF_ANOTHER_PRODUCT = price(BRAND_ID, OTHER_PRODUCT_ID, 2, 5,
            "2020-06-14T00:00:00", "2020-12-31T23:59:59", "9.99");

    private static final Price HIGHER_PRIORITY_OF_ANOTHER_BRAND = price(OTHER_BRAND_ID, PRODUCT_ID, 3, 5,
            "2020-06-14T00:00:00", "2020-12-31T23:59:59", "8.88");

    private static final Price HIGHER_PRIORITY_OUTSIDE_THE_INSTANT = price(BRAND_ID, PRODUCT_ID, 4, 5,
            "2020-06-15T00:00:00", "2020-06-15T23:59:59", "7.77");

    private static final Price EXPECTED_WINNER = price(BRAND_ID, PRODUCT_ID, 1, 1, "2020-06-14T00:00:00",
            "2020-12-31T23:59:59", "35.50");

    private static final List<Price> PRICES = List.of(
            LOWER_PRIORITY_COVERING_THE_INSTANT,
            HIGHER_PRIORITY_OF_ANOTHER_PRODUCT,
            HIGHER_PRIORITY_OF_ANOTHER_BRAND,
            HIGHER_PRIORITY_OUTSIDE_THE_INSTANT,
            EXPECTED_WINNER);

    private final FindApplicablePriceService service = new FindApplicablePriceService(
            new InMemoryPriceRepository(PRICES), new ApplicablePriceSelector());

    @Test
    @DisplayName("devuelve la tarifa de la cadena y el producto consultados que cubre el instante")
    void returns_the_tariff_of_the_requested_brand_and_product_covering_the_instant() {
        Price applicablePrice = service.findApplicablePrice(QUERY);

        assertThat(applicablePrice.priceList()).isEqualTo(1);
        assertThat(applicablePrice.amount()).isEqualByComparingTo("35.50");
    }

    @Test
    @DisplayName("propaga la ausencia de tarifa aplicable para la cadena y el producto consultados")
    void propagates_the_absence_of_an_applicable_tariff() {
        FindApplicablePriceService serviceWithoutPrices = new FindApplicablePriceService(
                new InMemoryPriceRepository(List.of()), new ApplicablePriceSelector());

        assertThatExceptionOfType(PriceNotFoundException.class)
                .isThrownBy(() -> serviceWithoutPrices.findApplicablePrice(QUERY));
    }

    private static Price price(long brandId, long productId, int priceList, int priority,
            String startsAt, String endsAt, String amount) {
        return new Price(brandId, productId, priceList, priority, new BigDecimal(amount), CURRENCY,
                new DateRange(LocalDateTime.parse(startsAt), LocalDateTime.parse(endsAt)));
    }
}
