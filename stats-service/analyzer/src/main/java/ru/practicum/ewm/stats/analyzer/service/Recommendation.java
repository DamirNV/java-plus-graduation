package ru.practicum.ewm.stats.analyzer.service;

public record Recommendation(
        long eventId,
        double score
) {
}
