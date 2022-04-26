package org.rhyssaldanha.time;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
            assertThrowsWithMessage(NullPointerException.class, "start must not be null", () -> Timespan.of(null, END));
            assertThrowsWithMessage(NullPointerException.class, "end must not be null", () -> Timespan.of(START, null));

            assertThrowsWithMessage(NullPointerException.class, "start must not be null", () -> Timespan.from(null, DURATION));
            assertThrowsWithMessage(NullPointerException.class, "duration must not be null", () -> Timespan.from(START, null));
        }

        @Test
        @DisplayName("end must come after start")
        void endMustComeAfterStart() {
            final Instant invalidEnd = START.minus(Duration.ofDays(10));

            assertThrowsWithMessage(DateTimeException.class, "end must not be before start", () -> Timespan.of(START, invalidEnd));
        }

        @Test
        @DisplayName("a zero length timespan is valid")
        void zeroLengthTimespanIsValid() {
            assertNotNull(Timespan.of(START, START));
            assertNotNull(Timespan.from(START, Duration.ZERO));
        }
    }

    @Nested
    @DisplayName("With timespan")
    class WithTimespan {
        private final Timespan TIMESPAN = Timespan.of(START, END);

        @Test
        @DisplayName("a timespan has a duration")
        void hasDuration() {
            assertEquals(DURATION, TIMESPAN.duration());
        }

        @Nested
        @DisplayName("With instants outside of timespan")
        class WithInstantsOutsideTimespan {
            private final Instant BEFORE = START.minus(Duration.ofHours(2));
            private final Instant MIDDLE = START.plus(Duration.ofHours(2));
            private final Instant AFTER = END.plus(Duration.ofHours(2));

            @Nested
            class Contains {
                @Test
                @DisplayName("null parameters are invalid")
                void notNullParameters() {
                    assertThrowsWithMessage(NullPointerException.class, "instant must not be null", () -> TIMESPAN.contains(null));
                }

                @Test
                @DisplayName("timespan contains instant")
                void containsInstant() {
                    assertTrue(TIMESPAN.contains(MIDDLE));
                }

                @Test
                @DisplayName("timespan does not contain instant")
                void doesNotContainInstant() {
                    assertFalse(TIMESPAN.contains(BEFORE));
                    assertFalse(TIMESPAN.contains(AFTER));
                }

                @Test
                @DisplayName("timespan is start inclusive")
                void startInclusive() {
                    assertTrue(TIMESPAN.contains(START));
                }

                @Test
                @DisplayName("timespan is end exclusive")
                void endExclusive() {
                    assertFalse(TIMESPAN.contains(END));
                }
            }

            @Nested
            class Split {
                @Test
                @DisplayName("null parameters are invalid")
                void notNullParameters() {
                    assertThrowsWithMessage(NullPointerException.class, "end must not be null", () -> TIMESPAN.to(null));
                    assertThrowsWithMessage(NullPointerException.class, "start must not be null", () -> TIMESPAN.from(null));
                }

                @Test
                @DisplayName("timespan can be split with an instant")
                void canSplit() {
                    assertEquals(Timespan.of(START, MIDDLE), TIMESPAN.to(MIDDLE));
                    assertEquals(Timespan.of(MIDDLE, END), TIMESPAN.from(MIDDLE));
                }

                @Test
                @DisplayName("cannot split if instant is outside timespan")
                void cannotSplit() {
                    assertThrowsWithMessage(DateTimeException.class, "end must not be before start", () -> TIMESPAN.from(AFTER));
                    assertThrowsWithMessage(DateTimeException.class, "end must not be before start", () -> TIMESPAN.to(BEFORE));
                }
            }
        }
    }

    @Test
    @DisplayName("value-based equality")
    void valueBasedEquality() {
        assertEquals(Timespan.of(START, END), Timespan.of(START, END));
        assertEquals(Timespan.of(START, END), Timespan.from(START, DURATION));
    }

    private static <T extends Throwable> void assertThrowsWithMessage(final Class<T> expectedType, final String expectedMessage, final Executable delegate) {
        final T exception = assertThrows(expectedType, delegate);
        assertEquals(expectedMessage, exception.getMessage());
    }
}