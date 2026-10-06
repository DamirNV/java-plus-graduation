package ru.practicum.ewm.stats.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.stats.analyzer.model.EventSimilarity;

import java.util.Collection;
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

    @Query("""
            select similarity
            from EventSimilarity similarity
            where similarity.eventA in :eventIds
               or similarity.eventB in :eventIds
            """)
    List<EventSimilarity> findAllByEventIds(
            @Param("eventIds") Collection<Long> eventIds
    );
}
