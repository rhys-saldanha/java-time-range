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
import java.util.function.Predicate;

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
    @JsonProperty("end")
    private final Optional<Instant> maybeEnd;

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
                                   @JsonProperty("end") final Optional<Instant> maybeEnd) {
        requireNonNull(start, "start must not be null");
        requireInstantsOrdered(start, maybeEnd);

        return new Timespan(start, maybeEnd);
    }

    private static void requireInstantsOrdered(final Instant start, final Optional<Instant> maybeEnd) {
        if (!instantsOrdered(start, maybeEnd)) {
            throw new DateTimeException("end must not be before start");
        }
    }

    private static Boolean instantsOrdered(final Instant start, final Optional<Instant> maybeEnd) {
        final Predicate<Instant> isBefore = start::isBefore;
        final Predicate<Instant> isEqualToStart = start::equals;
        return maybeEnd.stream().allMatch(isBefore.or(isEqualToStart));
    }

    private Timespan(final Instant start, final Optional<Instant> maybeEnd) {
        this.start = start;
        this.maybeEnd = maybeEnd;
    }

    @JsonGetter
    public Optional<Duration> duration() {
        return maybeEnd.map(end -> Duration.between(start, end));
    }

    public boolean contains(final Instant instant) {
        requireNonNull(instant, "instant must not be null");
        return isAfterStart(instant) && isBeforeEnd(instant);
    }

    private boolean isAfterStart(final Instant instant) {
        return instant.equals(start) || instant.isAfter(start);
    }

    private boolean isBeforeEnd(final Instant instant) {
        return maybeEnd.isEmpty() || instant.isBefore(maybeEnd.get());
    }

    public Timespan to(final Instant end) {
        requireNonNull(end, "end must not be null");
        requireContained(end, "end must be within existing timespan");
        return create(start, Optional.of(end));
    }

    public Timespan from(final Instant start) {
        requireNonNull(start, "start must not be null");
        requireContained(start, "start must be within existing timespan");
        return create(start, maybeEnd);
    }

    private void requireContained(final Instant instant, final String message) {
        if (!this.contains(instant)) {
            throw new DateTimeException(message);
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Timespan timespan = (Timespan) o;
        return start.equals(timespan.start) && maybeEnd.equals(timespan.maybeEnd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, maybeEnd);
    }

    @Override
    public String toString() {
        return "Timespan{" +
                "start=" + start +
                ", end=" + maybeEnd +
                '}';
    }
}
