package org.rhyssaldanha.time;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import static java.util.Objects.requireNonNull;

/**
 * This is a value-based class; programmers should treat instances that are
 * {@linkplain #equals(Object) equal} as interchangeable and should not
 * use instances for synchronization, or unpredictable behavior may
 * occur. For example, in a future release, synchronization may fail.
 * The {@code equals} method should be used for comparisons.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Timespan {

    @JsonProperty
    private final Instant start;
    @JsonProperty
    private final Instant end;

    @JsonCreator
    public static Timespan of(@JsonProperty("start") final Instant start,
                              @JsonProperty("end") final Instant end) {
        requireNonNull(start, "start must not be null");
        requireNonNull(end, "end must not be null");
        return create(start, end);
    }

    public static Timespan from(final Instant start, final Duration duration) {
        requireNonNull(start, "start must not be null");
        requireNonNull(duration, "duration must not be null");
        return create(start, start.plus(duration));
    }

    private static Timespan create(final Instant start, final Instant end) {
        if (end.isBefore(start)) {
            throw new DateTimeException("end must not be before start");
        }
        return new Timespan(start, end);
    }

    private Timespan(final Instant start, final Instant end) {
        this.start = start;
        this.end = end;
    }

    @JsonGetter
    public Duration duration() {
        return Duration.between(start, end);
    }

    public boolean contains(final Instant instant) {
        requireNonNull(instant, "instant must not be null");
        return instant.equals(start) || instant.isAfter(start) && instant.isBefore(end);
    }

    public Timespan to(final Instant end) {
        return Timespan.of(start, end);
    }

    public Timespan from(final Instant start) {
        return Timespan.of(start, end);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Timespan timespan = (Timespan) o;
        return start.equals(timespan.start) && end.equals(timespan.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }
}
