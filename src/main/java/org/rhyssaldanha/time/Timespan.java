package org.rhyssaldanha.time;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static java.util.Objects.requireNonNull;

/**
 * This is a value-based class; programmers should treat instances that are
 * {@linkplain #equals(Object) equal} as interchangeable and should not
 * use instances for synchronization, or unpredictable behavior may
 * occur. For example, in a future release, synchronization may fail.
 * The {@code equals} method should be used for comparisons.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(NON_ABSENT)
public final class Timespan {

    @JsonProperty
    private final Instant start;
    @JsonProperty
    private final Optional<Instant> end;

    public static Timespan of(final Instant start, final Instant end) {
        requireNonNull(start, "start must not be null");
        requireNonNull(end, "end must not be null");
        return create(start, Optional.of(end));
    }

    public static Timespan from(final Instant start, final Duration duration) {
        requireNonNull(start, "start must not be null");
        requireNonNull(duration, "duration must not be null");
        return create(start, Optional.of(start.plus(duration)));
    }

    public static Timespan starting(final Instant start) {
        requireNonNull(start, "start must not be null");
        return create(start, Optional.empty());
    }

    @JsonCreator
    private static Timespan create(@JsonProperty("start") final Instant start,
                                   @JsonProperty("end") final Optional<Instant> end) {
        requireNonNull(start, "start must not be null");
        requireNonNull(end, "end must not be null");

        if (end.isPresent()) {
            if (end.get().isBefore(start)) {
                throw new DateTimeException("end must not be before start");
            }
        }
        return new Timespan(start, end);
    }

    private Timespan(final Instant start, final Optional<Instant> end) {
        this.start = start;
        this.end = end;
    }

    @JsonGetter
    public Optional<Duration> duration() {
        return end.map(instant -> Duration.between(start, instant));
    }

    public boolean contains(final Instant instant) {
        requireNonNull(instant, "instant must not be null");
        return isAfterStart(instant) && isBeforeEnd(instant);
    }

    private boolean isAfterStart(final Instant instant) {
        return instant.equals(start) || instant.isAfter(start);
    }

    private boolean isBeforeEnd(final Instant instant) {
        return end.isEmpty() || instant.isBefore(end.get());
    }

    public Timespan to(final Instant end) {
        requireNonNull(end, "end must not be null");
        return create(start, Optional.of(end));
    }

    public Timespan from(final Instant start) {
        requireNonNull(start, "start must not be null");
        return create(start, end);
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

    @Override
    public String toString() {
        return "Timespan{" +
                "start=" + start +
                ", end=" + end +
                '}';
    }
}
