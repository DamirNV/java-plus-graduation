package ru.practicum.ewm.stats.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.stats.analyzer.model.EventSimilarity;

import java.util.List;
import java.util.Optional;

public interface EventSimilarityRepository
        extends JpaRepository<EventSimilarity, Long> {

    Optional<EventSimilarity> findByEventAAndEventB(
            Long eventA,
            Long eventB
    );

    List<EventSimilarity> findByEventAOrEventB(
            Long eventA,
            Long eventB
    );
}
