package com.develocity.api.tests;

import java.time.OffsetDateTime;

final class Interval {
    private final OffsetDateTime start;
    private final OffsetDateTime end;

    Interval(OffsetDateTime start, OffsetDateTime end) {
        this.start = start;
        this.end = end;
    }

    public OffsetDateTime getStart() {
        return start;
    }

    public OffsetDateTime getEnd() {
        return end;
    }
}
