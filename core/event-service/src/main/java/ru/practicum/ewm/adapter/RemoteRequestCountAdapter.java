package ru.practicum.ewm.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.client.RequestClient;
import ru.practicum.ewm.port.RequestCountPort;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RemoteRequestCountAdapter
        implements RequestCountPort {

    private final RequestClient requestClient;

    @Override
    public long countConfirmedRequests(Long eventId) {
        return requestClient.countConfirmedRequests(eventId);
    }

    @Override
    public Map<Long, Long> countConfirmedRequests(
            Collection<Long> eventIds
    ) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Map.of();
        }

        return requestClient.countConfirmedRequests(
                List.copyOf(eventIds)
        );
    }
}