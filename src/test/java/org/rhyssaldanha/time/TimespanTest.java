package org.rhyssaldanha.time;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TimespanTest {

    private static final Instant START = Instant.parse("2020-02-08T09:00:00Z");
    private static final Duration DURATION = Duration.ofHours(5);
    private static final Instant END = START.plus(DURATION);

    @Nested
    class Create {
        @Test
        @DisplayName("using start and end instants")
        void startAndEndInstants() {
            assertNotNull(Timespan.of(START, END));
        }

        @Test
        @DisplayName("using start instant and duration")
        void startInstantAndDuration() {
            assertNotNull(Timespan.from(START, DURATION));
        }
    }

    @Nested
    class Preconditions {
        @Test
        @DisplayName("null parameters are invalid")
        void notNullParameters() {
            assertThrows(NullPointerException.class, () -> Timespan.of(START, null));
            assertThrows(NullPointerException.class, () -> Timespan.of(null, END));

            assertThrows(NullPointerException.class, () -> Timespan.from(null, DURATION));
            assertThrows(NullPointerException.class, () -> Timespan.from(START, null));
        }

        @Test
        @DisplayName("end must come after start")
        void endMustComeAfterStart() {
            final Instant invalidEnd = START.minus(Duration.ofDays(10));

            assertThrows(DateTimeException.class, () -> Timespan.of(START, invalidEnd));
        }

        @Test
        @DisplayName("a zero length timespan is valid")
        void zeroLengthTimespanIsValid() {
            assertNotNull(Timespan.of(START, START));
            assertNotNull(Timespan.from(START, Duration.ZERO));
            assertNotNull(Timespan.from(START));
        }
    }

    @Nested
    class WithTimespan {
        private final Timespan TIMESPAN = Timespan.of(START, END);

        @Test
        @DisplayName("a timespan has a duration")
        void hasDuration() {
            assertEquals(DURATION, TIMESPAN.duration());
        }
    }
}