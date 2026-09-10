package com.adolcc.prices.infrastructure.adapter.output.persistence;

import com.adolcc.prices.domain.model.DateRange;
import com.adolcc.prices.domain.model.Price;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PricePersistenceAdapterTest {

    private static final long BRAND_ID = 1L;
    private static final long PRODUCT_ID = 35455L;

    @Autowired
    private PriceJpaRepository priceJpaRepository;

    private PricePersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new PricePersistenceAdapter(priceJpaRepository);
    }

    @Test
    @DisplayName("recupera las cuatro tarifas del ejemplo para el producto y la cadena indicados")
    void retrieves_the_four_sample_tariffs_of_the_requested_brand_and_product() {
        List<Price> prices = adapter.findByBrandAndProduct(BRAND_ID, PRODUCT_ID);

        assertThat(prices).hasSize(4);
        assertThat(prices).extracting(Price::priceList).containsExactlyInAnyOrder(1, 2, 3, 4);
    }

    @Test
    @DisplayName("traslada la ventana de validez, el importe y la divisa de la fila")
    void maps_the_window_amount_and_currency_of_the_row() {
        List<Price> prices = adapter.findByBrandAndProduct(BRAND_ID, PRODUCT_ID);

        assertThat(prices)
                .filteredOn(price -> price.priceList() == 2)
                .singleElement()
                .satisfies(price -> {
                    assertThat(price.amount()).isEqualByComparingTo("25.45");
                    assertThat(price.currency()).isEqualTo("EUR");
                    assertThat(price.validity()).isEqualTo(new DateRange(
                            LocalDateTime.of(2020, 6, 14, 15, 0),
                            LocalDateTime.of(2020, 6, 14, 18, 30)));
                });
    }

    @Test
    @DisplayName("devuelve una lista vacía cuando el producto o la cadena no tienen tarifas")
    void returns_an_empty_list_when_the_product_has_no_tariffs() {
        assertThat(adapter.findByBrandAndProduct(BRAND_ID, 12345L)).isEmpty();
    }
}
