package ru.practicum.ewm.adapter;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.client.RequestClient;
import ru.practicum.ewm.port.RequestCountPort;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoteRequestCountAdapter
        implements RequestCountPort {

    private static final String REQUEST_SERVICE_RETRY = "requestService";

    private final RequestClient requestClient;

    @Override
    @Retry(
            name = REQUEST_SERVICE_RETRY,
            fallbackMethod = "countConfirmedRequestsFallback"
    )
    public long countConfirmedRequests(Long eventId) {
        return requestClient.countConfirmedRequests(eventId);
    }

    @Override
    @Retry(
            name = REQUEST_SERVICE_RETRY,
            fallbackMethod = "countConfirmedRequestsBatchFallback"
    )
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

    private long countConfirmedRequestsFallback(
            Long eventId,
            Throwable throwable
    ) {
        log.warn(
                "Request service is unavailable for event id={}. "
                        + "Using confirmedRequests=0",
                eventId,
                throwable
        );

        return 0L;
    }

    private Map<Long, Long> countConfirmedRequestsBatchFallback(
            Collection<Long> eventIds,
            Throwable throwable
    ) {
        log.warn(
                "Request service is unavailable for events {}. "
                        + "Using empty confirmed requests map",
                eventIds,
                throwable
        );

        return Map.of();
    }
}
