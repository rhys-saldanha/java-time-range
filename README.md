## Overview

This library serves to extend the Java 8 Date/Time API to represent the notion of a _timespan_.

A timespan is a period of time between two fixed points on the time-line.

## Usage

### Factory creation methods

Following from the creation strategy used by `java.time` classes, `Timespan`s can only be created
through static factory methods.

```java
import org.rhyssaldanha.time.Timespan;

import java.time.Duration;
import java.time.Instant;

class Main {
    public static void main(String[] args) {
        final Instant start = Instant.now();
        final Duration duration = Duration.ofDays(10);
        final Instant end = start.plus(duration);

        Timespan.of(start, end);
        Timespan.from(start, duration);
    }
}
```

### Splitting

A timespan can be split, creating a new shorter timespan.

```java
import org.rhyssaldanha.time.Timespan;

import java.time.Duration;
import java.time.Instant;

class Main {
    public static void main(String[] args) {
        final Instant A = Instant.now();
        final Instant B = A.plus(Duration.ofDays(5));
        final Instant C = A.plus(Duration.ofDays(10));

        final Timespan AC = Timespan.of(A, C);

        final Timespan AB = AC.to(B);
        final Timespan BC = AC.from(B);
    }
}
```

### Contains

Given a point on the time-line, we can check if that point exists inside a timespan.

```java
import org.rhyssaldanha.time.Timespan;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static java.time.temporal.ChronoUnit.DAYS;

class Main {
    public static void main(String[] args) {
        final Instant A = Instant.now();
        final Instant B = A.plus(Duration.ofDays(5));
        final Instant C = A.plus(Duration.ofDays(10));

        final Timespan AC = Timespan.of(A, C);
        final Timespan AB = Timespan.of(A, B);

        AC.contains(B); //true
        AB.contains(C); //false
    }
}
```

## License

This project is Apache License 2.0 - see the [LICENSE](LICENSE) file for details
