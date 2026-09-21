package ru.practicum.ewm.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.client.CommentClient;
import ru.practicum.ewm.port.CommentCountPort;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoteCommentCountAdapter
        implements CommentCountPort {

    private final CommentClient commentClient;

    @Override
    public long countPublishedComments(Long eventId) {
        try {
            return commentClient.countPublishedComments(eventId);
        } catch (Exception e) {
            log.warn(
                    "Cannot get comment count for event {}",
                    eventId,
                    e
            );

            return 0L;
        }
    }

    @Override
    public Map<Long, Long> countPublishedComments(
            Collection<Long> eventIds
    ) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Map.of();
        }

        try {
            return commentClient.countPublishedComments(
                    List.copyOf(eventIds)
            );
        } catch (Exception e) {
            log.warn(
                    "Cannot get comment counts for events {}",
                    eventIds,
                    e
            );

            return Map.of();
        }
    }
}