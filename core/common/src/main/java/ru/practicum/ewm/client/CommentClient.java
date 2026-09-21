package ru.practicum.ewm.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "ewm-service", path = "/internal/comments")
public interface CommentClient {

    @GetMapping("/published/count")
    long countPublishedComments(
            @RequestParam("eventId") Long eventId
    );

    @GetMapping("/published/counts")
    Map<Long, Long> countPublishedComments(
            @RequestParam("eventIds") List<Long> eventIds
    );
}