package org.rhyssaldanha.time;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public final class Timespan {

    private final Instant startInclusive;
    private final Instant endExclusive;

    public static Timespan of(final Instant startInclusive, final Instant endExclusive) {
        Objects.requireNonNull(startInclusive, "startInclusive");
        Objects.requireNonNull(endExclusive, "endExclusive");
        return create(startInclusive, endExclusive);
    }

    public static Timespan from(final Instant startInclusive) {
        return from(startInclusive, Duration.ZERO);
    }

    public static Timespan from(final Instant startInclusive, final Duration duration) {
        Objects.requireNonNull(startInclusive, "startInclusive");
        Objects.requireNonNull(duration, "duration");
        return create(startInclusive, startInclusive.plus(duration));
    }

    private static Timespan create(final Instant startInclusive, final Instant endExclusive) {
        if (endExclusive.isBefore(startInclusive)) {
            throw new DateTimeException("End of timespan must occur after start");
        }
        return new Timespan(startInclusive, endExclusive);
    }

    private Timespan(final Instant startInclusive, final Instant endExclusive) {
        this.startInclusive = startInclusive;
        this.endExclusive = endExclusive;
    }

    public Duration duration() {
        return Duration.between(startInclusive, endExclusive);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Timespan timespan = (Timespan) o;
        return startInclusive.equals(timespan.startInclusive) && endExclusive.equals(timespan.endExclusive);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startInclusive, endExclusive);
    }
}
