package ru.practicum.ewm.stats.analyzer.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.stats.analyzer.model.UserInteraction;

import java.util.List;
import java.util.Optional;

public interface UserInteractionRepository
        extends JpaRepository<UserInteraction, Long> {

    Optional<UserInteraction> findByUserIdAndEventId(
            Long userId,
            Long eventId
    );

    List<UserInteraction> findByUserId(Long userId);

    List<UserInteraction> findByEventId(Long eventId);

    List<UserInteraction> findByUserIdOrderByLastInteractionAtDesc(
            Long userId,
            Pageable pageable
    );
}
