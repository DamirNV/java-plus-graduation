package ru.practicum.ewm.stats.analyzer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.analyzer.model.EventSimilarity;
import ru.practicum.ewm.stats.analyzer.model.UserInteraction;
import ru.practicum.ewm.stats.analyzer.repository.EventSimilarityRepository;
import ru.practicum.ewm.stats.analyzer.repository.UserInteractionRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private final UserInteractionRepository interactionRepository;
    private final EventSimilarityRepository similarityRepository;

    @Value("${stats.recommendations.neighbor-count:10}")
    private int neighborCount;

    public List<Recommendation> getSimilarEvents(
            long eventId,
            long userId,
            int maxResults
    ) {
        if (maxResults <= 0) {
            return List.of();
        }

        Set<Long> interactedEvents = new HashSet<>();

        for (UserInteraction interaction
                : interactionRepository.findByUserId(userId)) {
            interactedEvents.add(interaction.getEventId());
        }

        List<Recommendation> result = new ArrayList<>();

        for (EventSimilarity similarity
                : similarityRepository.findByEventAOrEventB(
                        eventId,
                        eventId
                )) {

            long candidate =
                    otherEvent(similarity, eventId);

            if (!interactedEvents.contains(candidate)) {
                result.add(
                        new Recommendation(
                                candidate,
                                similarity.getScore()
                        )
                );
            }
        }

        result.sort(
                Comparator
                        .comparingDouble(Recommendation::score)
                        .reversed()
                        .thenComparingLong(Recommendation::eventId)
        );

        return result.stream()
                .limit(maxResults)
                .toList();
    }

    public List<Recommendation> getRecommendations(
            long userId,
            int maxResults
    ) {
        if (maxResults <= 0) {
            return List.of();
        }

        List<UserInteraction> interactions =
                interactionRepository.findByUserId(userId);

        if (interactions.isEmpty()) {
            return List.of();
        }

        List<UserInteraction> recentInteractions =
                interactionRepository
                        .findByUserIdOrderByLastInteractionAtDesc(
                                userId,
                                PageRequest.of(0, maxResults)
                        );

        Set<Long> interactedEvents = new HashSet<>();
        Map<Long, Double> userRatings = new HashMap<>();

        for (UserInteraction interaction : interactions) {
            interactedEvents.add(interaction.getEventId());
            userRatings.put(
                    interaction.getEventId(),
                    interaction.getRating()
            );
        }

        Map<Long, List<EventSimilarity>> similaritiesByEvent =
                loadSimilarities(interactedEvents);

        Map<Long, Double> candidates = new HashMap<>();

        for (UserInteraction recent : recentInteractions) {
            long recentEventId = recent.getEventId();

            for (EventSimilarity similarity
                    : similaritiesByEvent.getOrDefault(
                            recentEventId,
                            List.of()
                    )) {

                long candidate =
                        otherEvent(
                                similarity,
                                recentEventId
                        );

                if (interactedEvents.contains(candidate)) {
                    continue;
                }

                candidates.merge(
                        candidate,
                        similarity.getScore(),
                        Math::max
                );
            }
        }

        List<Map.Entry<Long, Double>> orderedCandidates =
                new ArrayList<>(candidates.entrySet());

        orderedCandidates.sort(
                Map.Entry
                        .<Long, Double>comparingByValue()
                        .reversed()
                        .thenComparing(Map.Entry.comparingByKey())
        );

        List<Recommendation> recommendations =
                new ArrayList<>();

        int limit =
                Math.min(
                        maxResults,
                        orderedCandidates.size()
                );

        for (int index = 0; index < limit; index++) {
            long candidate =
                    orderedCandidates.get(index).getKey();

            Double predictedRating =
                    predictRating(
                            candidate,
                            userRatings,
                            similaritiesByEvent
                    );

            if (predictedRating != null) {
                recommendations.add(
                        new Recommendation(
                                candidate,
                                predictedRating
                        )
                );
            }
        }

        recommendations.sort(
                Comparator
                        .comparingDouble(Recommendation::score)
                        .reversed()
                        .thenComparingLong(Recommendation::eventId)
        );

        return recommendations;
    }

    public List<Recommendation> getInteractionCounts(
            List<Long> eventIds
    ) {
        if (eventIds.isEmpty()) {
            return List.of();
        }

        Map<Long, Double> ratingsByEvent = new HashMap<>();

        for (UserInteraction interaction
                : interactionRepository.findByEventIdIn(eventIds)) {
            ratingsByEvent.merge(
                    interaction.getEventId(),
                    interaction.getRating(),
                    Double::sum
            );
        }

        List<Recommendation> result = new ArrayList<>();

        for (Long eventId : eventIds) {
            result.add(
                    new Recommendation(
                            eventId,
                            ratingsByEvent.getOrDefault(eventId, 0.0)
                    )
            );
        }

        return result;
    }

    private Map<Long, List<EventSimilarity>> loadSimilarities(
            Set<Long> eventIds
    ) {
        Map<Long, List<EventSimilarity>> similaritiesByEvent =
                new HashMap<>();

        if (eventIds.isEmpty()) {
            return similaritiesByEvent;
        }

        for (EventSimilarity similarity
                : similarityRepository.findAllByEventIds(eventIds)) {

            similaritiesByEvent
                    .computeIfAbsent(
                            similarity.getEventA(),
                            ignored -> new ArrayList<>()
                    )
                    .add(similarity);

            similaritiesByEvent
                    .computeIfAbsent(
                            similarity.getEventB(),
                            ignored -> new ArrayList<>()
                    )
                    .add(similarity);
        }

        return similaritiesByEvent;
    }

    private Double predictRating(
            long candidateEventId,
            Map<Long, Double> userRatings,
            Map<Long, List<EventSimilarity>> similaritiesByEvent
    ) {
        List<Neighbor> neighbors = new ArrayList<>();

        for (EventSimilarity similarity
                : similaritiesByEvent.getOrDefault(
                        candidateEventId,
                        List.of()
                )) {

            long neighborEventId =
                    otherEvent(
                            similarity,
                            candidateEventId
                    );

            Double rating =
                    userRatings.get(neighborEventId);

            if (rating == null
                    || similarity.getScore() <= 0.0) {
                continue;
            }

            neighbors.add(
                    new Neighbor(
                            rating,
                            similarity.getScore()
                    )
            );
        }

        neighbors.sort(
                Comparator
                        .comparingDouble(Neighbor::similarity)
                        .reversed()
        );

        int limit =
                Math.min(
                        Math.max(neighborCount, 1),
                        neighbors.size()
                );

        double weightedRatings = 0.0;
        double similarities = 0.0;

        for (int index = 0; index < limit; index++) {
            Neighbor neighbor = neighbors.get(index);

            weightedRatings +=
                    neighbor.rating()
                            * neighbor.similarity();

            similarities += neighbor.similarity();
        }

        if (similarities == 0.0) {
            return null;
        }

        return weightedRatings / similarities;
    }

    private long otherEvent(
            EventSimilarity similarity,
            long eventId
    ) {
        if (similarity.getEventA() == eventId) {
            return similarity.getEventB();
        }

        return similarity.getEventA();
    }

    private record Neighbor(
            double rating,
            double similarity
    ) {
    }
}
