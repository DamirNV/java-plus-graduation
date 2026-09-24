package ru.practicum.ewm.port;

import java.util.Collection;
import java.util.Map;

public interface CommentCountPort {

    long countPublishedComments(Long eventId);

    Map<Long, Long> countPublishedComments(Collection<Long> eventIds);
}