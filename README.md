## Overview

This library serves to extend the Java 8 Date/Time API to represent the notion of a _timespan_.

A timespan is a period of time between two fixed points on the time-line.
This library also allows the representation of a start-only timespan.

## Usage

### Factory creation methods

Following from the creation strategy used by `java.time` classes, `Timespan`s can only be created
through static factory methods.

```java
import org.rhyssaldanha.time.Timespan;

import java.time.Duration;
import java.time.Instant;

class Create {
    public static void main(String[] args) {
        final Instant start = Instant.now();
        final Duration duration = Duration.ofDays(10);
        final Instant end = start.plus(duration);

        Timespan.of(start, end);
        Timespan.from(start, duration);
        Timespan.starting(start);
    }
}
```

### Splitting

A timespan can be split, creating a new shorter timespan.

```java
import org.rhyssaldanha.time.Timespan;

import java.time.Duration;
import java.time.Instant;

class Split {
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

class Contains {
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

### Jackson de/serialisation

A timespan can be serialised and deserialised.

In order to serialise `Instant`s, the following Jackson datatype package is required:

```xml
<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr310</artifactId>
</dependency>
```

```java
import org.rhyssaldanha.time.Timespan;

import java.time.Instant;

class Json {
    public static void main(String[] args) {
        Timespan.of(Instant.parse("2020-02-08T09:00:00Z"), Instant.parse("2020-02-08T14:00:00Z"));
    }
}
```

... will serialise to ...

```json5
{
  //UTC ISO-8601 format
  "start": "2020-02-08T09:00:00Z",
  "end": "2020-02-08T14:00:00Z",
  //seconds
  "duration": 18000.0
}
```

In order to correctly serialise `Optional` properties, the following Jackson datatype package is required:

```xml
<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jdk8</artifactId>
</dependency>
```

```java
import org.rhyssaldanha.time.Timespan;

import java.time.Instant;

class Json {
    public static void main(String[] args) {
        Timespan.starting(Instant.parse("2020-02-08T09:00:00Z"));
    }
}
```

... will serialise to ...

```json5
{
  "start": "2020-02-08T09:00:00Z"
}
```

## License

This project is Apache License 2.0 - see the [LICENSE](LICENSE) file for details
