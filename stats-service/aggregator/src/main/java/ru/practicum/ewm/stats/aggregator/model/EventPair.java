package ru.practicum.ewm.stats.aggregator.model;

public record EventPair(long eventA, long eventB) {

    public EventPair {
        if (eventA >= eventB) {
            throw new IllegalArgumentException(
                    "eventA must be less than eventB"
            );
        }
    }

    public static EventPair of(long first, long second) {
        if (first == second) {
            throw new IllegalArgumentException(
                    "An event cannot be paired with itself"
            );
        }

        return new EventPair(
                Math.min(first, second),
                Math.max(first, second)
        );
    }
}
