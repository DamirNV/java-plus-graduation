package ru.practicum.ewm.stats.aggregator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.aggregator.model.EventPair;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SimilarityService {

    private final SimilarityProducer producer;

    private final Map<Long, Map<Long, Double>> eventUserWeights =
            new HashMap<>();

    private final Map<Long, Double> eventWeightSums =
            new HashMap<>();

    private final Map<EventPair, Double> minWeightSums =
            new HashMap<>();

    public synchronized void process(UserActionAvro action) {
        long eventId = action.getEventId();
        long userId = action.getUserId();

        double incomingWeight =
                ActionWeight.of(action.getActionType());

        Map<Long, Double> userWeights =
                eventUserWeights.computeIfAbsent(
                        eventId,
                        ignored -> new HashMap<>()
                );

        double oldWeight =
                userWeights.getOrDefault(userId, 0.0);

        double newWeight =
                Math.max(oldWeight, incomingWeight);

        if (Double.compare(oldWeight, newWeight) == 0) {
            return;
        }

        userWeights.put(userId, newWeight);

        double newEventWeightSum =
                eventWeightSums.getOrDefault(eventId, 0.0)
                        + newWeight
                        - oldWeight;

        eventWeightSums.put(
                eventId,
                newEventWeightSum
        );

        for (Map.Entry<Long, Map<Long, Double>> entry
                : eventUserWeights.entrySet()) {

            long otherEventId = entry.getKey();

            if (otherEventId == eventId) {
                continue;
            }

            double otherWeight =
                    entry.getValue().getOrDefault(userId, 0.0);

            if (otherWeight == 0.0) {
                continue;
            }

            EventPair pair =
                    EventPair.of(eventId, otherEventId);

            double oldContribution =
                    Math.min(oldWeight, otherWeight);

            double newContribution =
                    Math.min(newWeight, otherWeight);

            double minWeightSum =
                    minWeightSums.getOrDefault(pair, 0.0)
                            + newContribution
                            - oldContribution;

            minWeightSums.put(
                    pair,
                    minWeightSum
            );

            double otherEventWeightSum =
                    eventWeightSums.getOrDefault(
                            otherEventId,
                            0.0
                    );

            double denominator =
                    Math.sqrt(
                            newEventWeightSum
                                    * otherEventWeightSum
                    );

            if (denominator == 0.0) {
                continue;
            }

            double similarity =
                    minWeightSum / denominator;

            EventSimilarityAvro result =
                    EventSimilarityAvro.newBuilder()
                            .setEventA(pair.eventA())
                            .setEventB(pair.eventB())
                            .setScore(similarity)
                            .setTimestamp(action.getTimestamp())
                            .build();

            producer.send(result);
        }
    }
}
