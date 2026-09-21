package ru.practicum.ewm.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.model.RequestStatus;
import ru.practicum.ewm.port.RequestCountPort;
import ru.practicum.ewm.repository.RequestRepository;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RequestCountAdapter implements RequestCountPort {

    private final RequestRepository requestRepository;

    @Override
    public long countConfirmedRequests(Long eventId) {
        return requestRepository.countByEventIdAndStatus(
                eventId,
                RequestStatus.CONFIRMED
        );
    }

    @Override
    public Map<Long, Long> countConfirmedRequests(Collection<Long> eventIds) {
        return requestRepository.countByEventIdsAndStatus(
                        eventIds,
                        RequestStatus.CONFIRMED
                ).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }
}