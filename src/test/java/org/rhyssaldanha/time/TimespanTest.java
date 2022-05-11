package org.rhyssaldanha.time;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

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
        @DisplayName("can create timespan using start and end instants")
        void startAndEndInstants() {
            assertNotNull(Timespan.of(START, END));
        }

        @Test
        @DisplayName("can create timespan using start instant and duration")
        void startInstantAndDuration() {
            assertNotNull(Timespan.from(START, DURATION));
        }

        @Test
        @DisplayName("can create timespan with no defined end")
        void undefinedEnd() {
            assertNotNull(Timespan.starting(START));
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

            assertThrowsWithMessage(NullPointerException.class, "start must not be null", () -> Timespan.starting(null));
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
            assertEquals(Optional.of(DURATION), TIMESPAN.duration());
        }

        @Nested
        @DisplayName("With times before, during and after timespan")
        class WithInstantsOutsideTimespan {
            private final Instant BEFORE = START.minus(Duration.ofHours(2));
            private final Instant DURING = START.plus(Duration.ofHours(2));
            private final Instant AFTER = END.plus(Duration.ofHours(2));

            @Nested
            class Contains {
                @Test
                @DisplayName("null parameters are invalid")
                void notNullParameters() {
                    assertThrowsWithMessage(NullPointerException.class, "instant must not be null", () -> TIMESPAN.contains(null));
                }

                @Test
                @DisplayName("contains time during timespan")
                void containsInstant() {
                    assertTrue(TIMESPAN.contains(DURING));
                }

                @Test
                @DisplayName("does not contain times before or after timespan")
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
                @DisplayName("can be split with a time during timespan")
                void canSplit() {
                    assertEquals(Timespan.of(DURING, END), TIMESPAN.from(DURING));
                    assertEquals(Timespan.of(START, DURING), TIMESPAN.to(DURING));
                }

                @Test
                @DisplayName("cannot be split if time is outside timespan")
                void cannotSplit() {
                    assertThrowsWithMessage(DateTimeException.class, "start must be within existing timespan", () -> TIMESPAN.from(BEFORE));
                    assertThrowsWithMessage(DateTimeException.class, "start must be within existing timespan", () -> TIMESPAN.from(AFTER));

                    assertThrowsWithMessage(DateTimeException.class, "end must be within existing timespan", () -> TIMESPAN.to(BEFORE));
                    assertThrowsWithMessage(DateTimeException.class, "end must be within existing timespan", () -> TIMESPAN.to(AFTER));
                }
            }

            @Nested
            class Json {
                private final ObjectMapper objectMapper = JsonMapper.builder()
                        .findAndAddModules()
                        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                        .build();

                @Test
                @DisplayName("can serialise")
                void serialise() throws Exception {
                    final String actualJson = objectMapper.writeValueAsString(TIMESPAN);
                    final String expectedJson = Files.readString(Paths.get("src", "test", "resources", "timespan.json"));

                    JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.STRICT);
                }

                @Test
                @DisplayName("can deserialise")
                void deserialise() throws Exception {
                    final String json = Files.readString(Paths.get("src", "test", "resources", "timespan.json"));
                    final Timespan actualTimespan = objectMapper.readValue(json, Timespan.class);

                    assertEquals(TIMESPAN, actualTimespan);
                }
            }
        }
    }

    @Nested
    @DisplayName("With start-only timespan")
    class WithStartTimespan {
        private final Timespan TIMESPAN = Timespan.starting(START);

        @Test
        @DisplayName("timespan has no duration")
        void hasDuration() {
            assertEquals(Optional.empty(), TIMESPAN.duration());
        }

        @Nested
        @DisplayName("With times before and after start")
        class WithInstantsOutsideTimespan {
            private final Instant BEFORE = START.minus(Duration.ofHours(2));
            private final Instant DURING = START.plus(Duration.ofHours(2));

            @Nested
            class Contains {
                @Test
                @DisplayName("contains time after start")
                void containsInstant() {
                    assertTrue(TIMESPAN.contains(DURING));
                }

                @Test
                @DisplayName("does not contain time before start")
                void doesNotContainInstant() {
                    assertFalse(TIMESPAN.contains(BEFORE));
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
                @DisplayName("timespan can be split")
                void canSplit() {
                    assertEquals(Timespan.starting(DURING), TIMESPAN.from(DURING));
                    assertEquals(Timespan.of(START, DURING), TIMESPAN.to(DURING));
                }

                @Test
                @DisplayName("cannot split if instant is outside timespan")
                void cannotSplit() {
                    assertThrowsWithMessage(DateTimeException.class, "start must be within existing timespan", () -> TIMESPAN.from(BEFORE));
                    assertThrowsWithMessage(DateTimeException.class, "end must be within existing timespan", () -> TIMESPAN.to(BEFORE));
                }
            }

            @Nested
            class Json {
                private final ObjectMapper objectMapper = JsonMapper.builder()
                        .findAndAddModules()
                        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                        .build();

                @Test
                @DisplayName("can serialise")
                void serialise() throws Exception {
                    final String actualJson = objectMapper.writeValueAsString(TIMESPAN);
                    final String expectedJson = Files.readString(Paths.get("src", "test", "resources", "start-only-timespan.json"));

                    JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.STRICT);
                }

                @Test
                @DisplayName("can deserialise")
                void deserialise() throws Exception {
                    final String json = Files.readString(Paths.get("src", "test", "resources", "start-only-timespan.json"));
                    final Timespan actualTimespan = objectMapper.readValue(json, Timespan.class);

                    assertEquals(TIMESPAN, actualTimespan);
                }
            }
        }
    }

    @Test
    @DisplayName("value-based equality")
    void valueBasedEquality() {
        assertEquals(Timespan.of(START, END), Timespan.of(START, END));
        assertEquals(Timespan.of(START, END), Timespan.from(START, DURATION));
        assertEquals(Timespan.starting(START), Timespan.starting(START));
    }

    private static <T extends Throwable> void assertThrowsWithMessage(final Class<T> expectedType, final String expectedMessage, final Executable delegate) {
        final T exception = assertThrows(expectedType, delegate);
        assertEquals(expectedMessage, exception.getMessage());
    }
}