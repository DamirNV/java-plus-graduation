package ru.practicum.ewm.stats.analyzer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.analyzer.model.EventSimilarity;
import ru.practicum.ewm.stats.analyzer.model.UserInteraction;
import ru.practicum.ewm.stats.analyzer.repository.EventSimilarityRepository;
import ru.practicum.ewm.stats.analyzer.repository.UserInteractionRepository;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AnalyzerStorageService {

    private final UserInteractionRepository interactionRepository;
    private final EventSimilarityRepository similarityRepository;

    @Transactional
    public void saveUserAction(UserActionAvro action) {
        long userId = action.getUserId();
        long eventId = action.getEventId();

        UserInteraction interaction =
                interactionRepository
                        .findByUserIdAndEventId(userId, eventId)
                        .orElseGet(UserInteraction::new);

        interaction.setUserId(userId);
        interaction.setEventId(eventId);

        double actionWeight = weightOf(action.getActionType());

        if (interaction.getId() == null
                || actionWeight > interaction.getRating()) {
            interaction.setRating(actionWeight);
        }

        Instant actionTimestamp = action.getTimestamp();

        if (interaction.getLastInteractionAt() == null
                || actionTimestamp.isAfter(
                        interaction.getLastInteractionAt()
                )) {
            interaction.setLastInteractionAt(actionTimestamp);
        }

        interactionRepository.save(interaction);
    }

    @Transactional
    public void saveSimilarity(EventSimilarityAvro message) {
        long first = Math.min(
                message.getEventA(),
                message.getEventB()
        );

        long second = Math.max(
                message.getEventA(),
                message.getEventB()
        );

        if (first == second) {
            return;
        }

        EventSimilarity similarity =
                similarityRepository
                        .findByEventAAndEventB(first, second)
                        .orElseGet(EventSimilarity::new);

        Instant timestamp = message.getTimestamp();

        if (similarity.getUpdatedAt() != null
                && timestamp.isBefore(similarity.getUpdatedAt())) {
            return;
        }

        similarity.setEventA(first);
        similarity.setEventB(second);
        similarity.setScore(message.getScore());
        similarity.setUpdatedAt(timestamp);

        similarityRepository.save(similarity);
    }

    private double weightOf(ActionTypeAvro actionType) {
        return switch (actionType) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }
}
