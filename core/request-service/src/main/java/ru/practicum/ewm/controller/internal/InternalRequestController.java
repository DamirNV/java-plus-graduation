package ru.practicum.ewm.controller.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.model.RequestStatus;
import ru.practicum.ewm.repository.RequestRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/requests")
public class InternalRequestController {

    private final RequestRepository requestRepository;

    @GetMapping("/confirmed/count")
    public long countConfirmedRequests(
            @RequestParam Long eventId
    ) {
        return requestRepository.countByEventIdAndStatus(
                eventId,
                RequestStatus.CONFIRMED
        );
    }

    @GetMapping("/confirmed/counts")
    public Map<Long, Long> countConfirmedRequests(
            @RequestParam List<Long> eventIds
    ) {
        return requestRepository
                .countByEventIdsAndStatus(
                        eventIds,
                        RequestStatus.CONFIRMED
                )
                .stream()
                .collect(
                        Collectors.toMap(
                                row -> (Long) row[0],
                                row -> (Long) row[1]
                        )
                );
    }
}