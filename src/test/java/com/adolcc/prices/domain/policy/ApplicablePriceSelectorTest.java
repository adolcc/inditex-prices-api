package com.adolcc.prices.domain.policy;

import com.adolcc.prices.domain.exception.PriceNotFoundException;
import com.adolcc.prices.domain.model.DateRange;
import com.adolcc.prices.domain.model.Price;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ApplicablePriceSelectorTest {

        private static final long BRAND_ID = 1L;
        private static final long PRODUCT_ID = 35455L;
        private static final String CURRENCY = "EUR";
        private static final LocalDateTime INSTANT = LocalDateTime.of(2020, 6, 14, 11, 0);

        private static final Price CHEAPEST_AND_HIGHEST_PRICE_LIST = price(9, 1, "2020-04-14T00:00:00",
                        "2020-06-14T23:59:59", "10.00");

        private static final Price HIGHEST_PRIORITY_OUTSIDE_THE_INSTANT = price(7, 99, "2020-06-16T00:00:00",
                        "2020-06-16T23:59:59", "70.00");

        private static final Price WIDEST_OLDEST_AND_EXPECTED_WINNER = price(1, 2, "2020-01-01T00:00:00",
                        "2020-12-31T23:59:59", "30.00");

        private static final Price NARROWEST_RECENT_AND_MOST_EXPENSIVE = price(5, 1, "2020-06-14T10:00:00",
                        "2020-06-14T12:00:00", "50.00");

        private static final List<Price> PRICES = List.of(
                        CHEAPEST_AND_HIGHEST_PRICE_LIST,
                        HIGHEST_PRIORITY_OUTSIDE_THE_INSTANT,
                        WIDEST_OLDEST_AND_EXPECTED_WINNER,
                        NARROWEST_RECENT_AND_MOST_EXPENSIVE);

        private final ApplicablePriceSelector selector = new ApplicablePriceSelector();

        @Test
        @DisplayName("aplica la tarifa de mayor prioridad entre las que cubren el instante")
        void selects_the_highest_priority_tariff_covering_the_instant() {
                Price winner = selector.select(PRICES, INSTANT);

                assertThat(winner.priceList()).isEqualTo(1);
                assertThat(winner.amount()).isEqualByComparingTo("30.00");
        }

        @Test
        @DisplayName("la tarifa de máxima prioridad no cubre el instante de referencia")
        void highest_priority_tariff_does_not_cover_the_instant() {
                assertThat(HIGHEST_PRIORITY_OUTSIDE_THE_INSTANT.validity().contains(INSTANT)).isFalse();
        }

        @Test
        @DisplayName("no hay tarifa aplicable cuando ninguna cubre el instante")
        void rejects_the_search_when_no_tariff_covers_the_instant() {
                List<Price> pricesOutsideTheInstant = List.of(HIGHEST_PRIORITY_OUTSIDE_THE_INSTANT);

                assertThatExceptionOfType(PriceNotFoundException.class)
                                .isThrownBy(() -> selector.select(pricesOutsideTheInstant, INSTANT));
        }

        private static Price price(int priceList, int priority, String startsAt, String endsAt, String amount) {
                return new Price(BRAND_ID, PRODUCT_ID, priceList, priority, new BigDecimal(amount), CURRENCY,
                                new DateRange(LocalDateTime.parse(startsAt), LocalDateTime.parse(endsAt)));
        }
}
