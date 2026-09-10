package com.adolcc.prices.domain.model;

import java.time.LocalDateTime;

public record DateRange(LocalDateTime startsAt, LocalDateTime endsAt) {

    public DateRange {
        if (startsAt.isAfter(endsAt)) {
            throw new IllegalArgumentException("Una ventana de validez no puede empezar después de terminar");
        }
    }

    public boolean contains(LocalDateTime instant) {
        return !instant.isBefore(startsAt) && !instant.isAfter(endsAt);
    }
}
