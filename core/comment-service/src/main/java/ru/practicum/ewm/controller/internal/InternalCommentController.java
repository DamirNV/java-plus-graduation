package ru.practicum.ewm.controller.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.model.CommentStatus;
import ru.practicum.ewm.repository.CommentRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/comments")
public class InternalCommentController {

    private final CommentRepository commentRepository;

    @GetMapping("/published/count")
    public long countPublishedComments(
            @RequestParam Long eventId
    ) {
        return commentRepository.countByEventIdAndStatus(
                eventId,
                CommentStatus.PUBLISHED
        );
    }

    @GetMapping("/published/counts")
    public Map<Long, Long> countPublishedComments(
            @RequestParam List<Long> eventIds
    ) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Map.of();
        }

        return commentRepository
                .countByEventIdsAndStatus(
                        eventIds,
                        CommentStatus.PUBLISHED
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