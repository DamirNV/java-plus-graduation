package ru.practicum.ewm.controller.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.dto.EventInternalDto;
import ru.practicum.ewm.repository.EventRepository;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/events")
public class InternalEventController {

    private final EventRepository eventRepository;

    @GetMapping("/{eventId}")
    public EventInternalDto getEvent(
            @PathVariable Long eventId
    ) {
        return eventRepository.findById(eventId)
                .map(event ->
                        EventInternalDto.builder()
                                .id(event.getId())
                                .initiatorId(event.getInitiatorId())
                                .state(event.getState().name())
                                .participantLimit(
                                        event.getParticipantLimit()
                                )
                                .requestModeration(
                                        event.getRequestModeration()
                                )
                                .build()
                )
                .orElse(null);
    }
}