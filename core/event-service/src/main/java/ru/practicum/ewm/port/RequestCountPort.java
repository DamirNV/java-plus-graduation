package ru.practicum.ewm.port;

import java.util.Collection;
import java.util.Map;

public interface RequestCountPort {

    long countConfirmedRequests(Long eventId);

    Map<Long, Long> countConfirmedRequests(Collection<Long> eventIds);
}