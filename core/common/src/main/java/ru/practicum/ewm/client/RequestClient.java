package ru.practicum.ewm.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "request-service", path = "/internal/requests")
public interface RequestClient {

    @GetMapping("/confirmed/count")
    long countConfirmedRequests(
            @RequestParam("eventId") Long eventId
    );

    @GetMapping("/confirmed/counts")
    Map<Long, Long> countConfirmedRequests(
            @RequestParam("eventIds") List<Long> eventIds
    );
}