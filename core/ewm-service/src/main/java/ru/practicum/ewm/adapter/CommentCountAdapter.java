package ru.practicum.ewm.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.model.CommentStatus;
import ru.practicum.ewm.port.CommentCountPort;
import ru.practicum.ewm.repository.CommentRepository;

import java.util.Collection;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CommentCountAdapter implements CommentCountPort {

    private final CommentRepository commentRepository;

    @Override
    public long countPublishedComments(Long eventId) {
        return commentRepository.countByEventIdAndStatus(
                eventId,
                CommentStatus.PUBLISHED
        );
    }

    @Override
    public Map<Long, Long> countPublishedComments(Collection<Long> eventIds) {
        return commentRepository.countByEventIdsAndStatus(
                eventIds,
                CommentStatus.PUBLISHED
        );
    }
}