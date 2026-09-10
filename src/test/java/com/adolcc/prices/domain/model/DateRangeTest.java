package com.adolcc.prices.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class DateRangeTest {

    private static final LocalDateTime STARTS_AT = LocalDateTime.of(2020, 6, 14, 15, 0);
    private static final LocalDateTime ENDS_AT = LocalDateTime.of(2020, 6, 14, 18, 30);

    private final DateRange validity = new DateRange(STARTS_AT, ENDS_AT);

    @Test
    @DisplayName("el extremo inicial pertenece a la ventana de validez")
    void contains_its_start_instant() {
        assertThat(validity.contains(STARTS_AT)).isTrue();
    }

    @Test
    @DisplayName("el extremo final pertenece a la ventana de validez")
    void contains_its_end_instant() {
        assertThat(validity.contains(ENDS_AT)).isTrue();
    }

    @Test
    @DisplayName("un instante anterior al extremo inicial queda fuera de la ventana")
    void does_not_contain_an_instant_before_its_start() {
        assertThat(validity.contains(STARTS_AT.minusNanos(1))).isFalse();
    }

    @Test
    @DisplayName("un instante posterior al extremo final queda fuera de la ventana")
    void does_not_contain_an_instant_after_its_end() {
        assertThat(validity.contains(ENDS_AT.plusNanos(1))).isFalse();
    }

    @Test
    @DisplayName("no se puede construir una ventana que termine antes de empezar")
    void rejects_a_window_ending_before_it_starts() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new DateRange(ENDS_AT, STARTS_AT));
    }
}
