package ru.practicum.ewm.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.client.UserClient;
import ru.practicum.ewm.dto.CompilationDto;
import ru.practicum.ewm.dto.EventShortDto;
import ru.practicum.ewm.dto.NewCompilationDto;
import ru.practicum.ewm.dto.UpdateCompilationRequest;
import ru.practicum.ewm.dto.UserShortDto;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.CompilationMapper;
import ru.practicum.ewm.mapper.EventMapper;
import ru.practicum.ewm.model.Compilation;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.port.CommentCountPort;
import ru.practicum.ewm.port.RequestCountPort;
import ru.practicum.ewm.repository.CompilationRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.service.CompilationService;
import ru.practicum.ewm.service.StatsHelperService;
import ru.practicum.ewm.util.OffsetPageRequest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;
    private final EventMapper eventMapper;
    private final UserClient userClient;
    private final RequestCountPort requestCountPort;
    private final CommentCountPort commentCountPort;
    private final StatsHelperService statsHelperService;

    @Override
    public CompilationDto addCompilation(NewCompilationDto newCompilationDto) {
        log.info("Adding new compilation: {}", newCompilationDto.getTitle());
        Compilation compilation = compilationMapper.toEntity(newCompilationDto);
        compilation.setPinned(newCompilationDto.getPinned());

        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            List<Event> events = eventRepository.findAllById(newCompilationDto.getEvents());
            compilation.setEvents(events);
        } else {
            compilation.setEvents(new ArrayList<>());
        }

        compilation = compilationRepository.save(compilation);
        return toDtoWithEvents(compilation);
    }

    @Override
    public void deleteCompilation(Long compId) {
        log.info("Deleting compilation with id: {}", compId);
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Compilation with id=" + compId + " was not found");
        }
        compilationRepository.deleteById(compId);
    }

    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateRequest) {
        log.info("Updating compilation with id: {}", compId);
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(
                        "Compilation with id=" + compId + " was not found"
                ));

        if (updateRequest.getTitle() != null) {
            compilation.setTitle(updateRequest.getTitle());
        }
        if (updateRequest.getPinned() != null) {
            compilation.setPinned(updateRequest.getPinned());
        }
        if (updateRequest.getEvents() != null) {
            List<Event> events = eventRepository.findAllById(updateRequest.getEvents());
            compilation.setEvents(events);
        }

        compilation = compilationRepository.save(compilation);
        return toDtoWithEvents(compilation);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        Pageable pageable = new OffsetPageRequest(from, size);

        List<Compilation> compilations = pinned == null
                ? compilationRepository.findAll(pageable).getContent()
                : compilationRepository.findAllByPinned(pinned, pageable).getContent();

        Map<Long, EventShortDto> eventDtos = buildEventDtos(
                compilations.stream()
                        .filter(compilation -> compilation.getEvents() != null)
                        .flatMap(compilation -> compilation.getEvents().stream())
                        .toList()
        );

        return compilations.stream()
                .map(compilation -> toDtoWithEvents(compilation, eventDtos))
                .toList();
    }

    @Override
    public CompilationDto getCompilation(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(
                        "Compilation with id=" + compId + " was not found"
                ));

        return toDtoWithEvents(compilation);
    }

    private CompilationDto toDtoWithEvents(Compilation compilation) {
        List<Event> events = compilation.getEvents() == null
                ? List.of()
                : compilation.getEvents();

        return toDtoWithEvents(compilation, buildEventDtos(events));
    }

    private CompilationDto toDtoWithEvents(
            Compilation compilation,
            Map<Long, EventShortDto> eventDtos
    ) {
        CompilationDto dto = compilationMapper.toDto(compilation);

        if (compilation.getEvents() != null) {
            dto.setEvents(compilation.getEvents().stream()
                    .map(event -> eventDtos.get(event.getId()))
                    .filter(java.util.Objects::nonNull)
                    .toList());
        }

        return dto;
    }

    private Map<Long, EventShortDto> buildEventDtos(List<Event> sourceEvents) {
        if (sourceEvents == null || sourceEvents.isEmpty()) {
            return Map.of();
        }

        List<Event> events = new ArrayList<>(sourceEvents.stream()
                .collect(Collectors.toMap(
                        Event::getId,
                        Function.identity(),
                        (first, second) -> first,
                        LinkedHashMap::new
                ))
                .values());

        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .toList();

        List<Long> userIds = events.stream()
                .map(Event::getInitiatorId)
                .distinct()
                .toList();

        Map<Long, UserShortDto> users = userClient.getUsers(userIds).stream()
                .collect(Collectors.toMap(
                        UserShortDto::getId,
                        Function.identity()
                ));

        Map<Long, Long> confirmedRequests = requestCountPort.countConfirmedRequests(eventIds);
        Map<Long, Long> commentsCount = commentCountPort.countPublishedComments(eventIds);
        Map<Long, Long> views = statsHelperService.getViews(events);

        return events.stream()
                .collect(Collectors.toMap(
                        Event::getId,
                        event -> toShortDto(
                                event,
                                users.get(event.getInitiatorId()),
                                confirmedRequests.getOrDefault(event.getId(), 0L),
                                views.getOrDefault(event.getId(), 0L),
                                commentsCount.getOrDefault(event.getId(), 0L)
                        )
                ));
    }

    private EventShortDto toShortDto(
            Event event,
            UserShortDto initiator,
            long confirmedRequests,
            long views,
            long commentsCount
    ) {
        EventShortDto dto = eventMapper.toShortDto(event);
        dto.setInitiator(initiator);
        dto.setConfirmedRequests(confirmedRequests);
        dto.setViews(views);
        dto.setComments(commentsCount);
        return dto;
    }
}
