package ru.practicum.ewm.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.client.UserClient;
import ru.practicum.ewm.dto.CommentDto;
import ru.practicum.ewm.dto.NewCommentDto;
import ru.practicum.ewm.dto.UpdateCommentDto;
import ru.practicum.ewm.dto.UserShortDto;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.CommentMapper;
import ru.practicum.ewm.model.Comment;
import ru.practicum.ewm.model.CommentStatus;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.EventState;
import ru.practicum.ewm.repository.CommentRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.service.CommentService;
import ru.practicum.ewm.service.StatsHelperService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserClient userClient;
    private final EventRepository eventRepository;
    private final CommentMapper commentMapper;
    private final StatsHelperService statsHelperService;

    @Override
    public CommentDto addComment(
            Long userId,
            Long eventId,
            NewCommentDto newCommentDto
    ) {
        UserShortDto user = getUser(userId);
        Event event = getEvent(eventId);

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException(
                    "Only published events can be commented"
            );
        }

        Comment comment = commentMapper.toEntity(newCommentDto);
        comment.setAuthorId(user.getId());
        comment.setEvent(event);
        comment.setStatus(CommentStatus.PENDING);
        comment.setCreated(LocalDateTime.now());

        return toDto(commentRepository.save(comment));
    }

    @Override
    public List<CommentDto> getUserComments(
            Long userId,
            int from,
            int size
    ) {
        checkUserExists(userId);

        Pageable pageable = PageRequest.of(
                from / size,
                size
        );

        return commentRepository
                .findByAuthorId(userId, pageable)
                .getContent()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public CommentDto updateComment(
            Long userId,
            Long commentId,
            UpdateCommentDto updateCommentDto
    ) {
        checkUserExists(userId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Comment with id=" +
                                        commentId +
                                        " was not found"
                        )
                );

        if (!comment.getAuthorId().equals(userId)) {
            throw new NotFoundException(
                    "Comment with id=" +
                            commentId +
                            " was not found"
            );
        }

        if (comment.getStatus() == CommentStatus.PUBLISHED) {
            throw new ConflictException(
                    "Published comment cannot be updated"
            );
        }

        if (comment.getStatus() == CommentStatus.DELETED) {
            throw new ConflictException(
                    "Deleted comment cannot be updated"
            );
        }

        comment.setText(updateCommentDto.getText());
        comment.setUpdated(LocalDateTime.now());
        comment.setStatus(CommentStatus.PENDING);

        return toDto(commentRepository.save(comment));
    }

    @Override
    public void deleteComment(
            Long userId,
            Long commentId
    ) {
        checkUserExists(userId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Comment with id=" +
                                        commentId +
                                        " was not found"
                        )
                );

        if (!comment.getAuthorId().equals(userId)) {
            throw new NotFoundException(
                    "Comment with id=" +
                            commentId +
                            " was not found"
            );
        }

        comment.setStatus(CommentStatus.DELETED);
        comment.setUpdated(LocalDateTime.now());

        commentRepository.save(comment);
    }

    @Override
    public List<CommentDto> getEventComments(
            Long eventId,
            int from,
            int size,
            HttpServletRequest request
    ) {
        checkEventExists(eventId);

        Pageable pageable = PageRequest.of(
                from / size,
                size,
                Sort.by(
                        Sort.Direction.ASC,
                        "created"
                )
        );

        List<CommentDto> comments = commentRepository
                .findByEventIdAndStatus(
                        eventId,
                        CommentStatus.PUBLISHED,
                        pageable
                )
                .getContent()
                .stream()
                .map(this::toDto)
                .toList();

        statsHelperService.hit(request);

        return comments;
    }

    @Override
    public CommentDto getEventComment(
            Long eventId,
            Long commentId,
            HttpServletRequest request
    ) {
        Comment comment =
                commentRepository.findByIdAndEventId(
                                commentId,
                                eventId
                        )
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "Comment with id=" +
                                                commentId +
                                                " was not found"
                                )
                        );

        if (comment.getStatus() != CommentStatus.PUBLISHED) {
            throw new NotFoundException(
                    "Comment with id=" +
                            commentId +
                            " was not found"
            );
        }

        statsHelperService.hit(request);

        return toDto(comment);
    }

    @Override
    public List<CommentDto> getAllComments(
            String status,
            int from,
            int size
    ) {
        CommentStatus commentStatus =
                CommentStatus.from(status);

        Pageable pageable = PageRequest.of(
                from / size,
                size,
                Sort.by(
                        Sort.Direction.DESC,
                        "created"
                )
        );

        return commentRepository
                .findAllByStatus(
                        commentStatus,
                        pageable
                )
                .getContent()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public CommentDto publishComment(Long commentId) {
        Comment comment = getComment(commentId);

        if (comment.getStatus() != CommentStatus.PENDING) {
            throw new ConflictException(
                    "Only comment with status PENDING can be published"
            );
        }

        comment.setStatus(CommentStatus.PUBLISHED);
        comment.setUpdated(LocalDateTime.now());

        return toDto(
                commentRepository.save(comment)
        );
    }

    @Override
    public CommentDto rejectComment(Long commentId) {
        Comment comment = getComment(commentId);

        if (comment.getStatus() != CommentStatus.PENDING) {
            throw new ConflictException(
                    "Only comment with status PENDING can be rejected"
            );
        }

        comment.setStatus(CommentStatus.REJECTED);
        comment.setUpdated(LocalDateTime.now());

        return toDto(
                commentRepository.save(comment)
        );
    }

    @Override
    public void deleteCommentByAdmin(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new NotFoundException(
                    "Comment with id=" +
                            commentId +
                            " was not found"
            );
        }

        commentRepository.deleteById(commentId);
    }

    private CommentDto toDto(Comment comment) {
        CommentDto dto = commentMapper.toDto(comment);

        dto.setAuthor(
                getUser(comment.getAuthorId())
        );

        return dto;
    }

    private Comment getComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Comment with id=" +
                                        commentId +
                                        " was not found"
                        )
                );
    }

    private UserShortDto getUser(Long userId) {
        UserShortDto user =
                userClient.getUser(userId);

        if (user == null) {
            throw new NotFoundException(
                    "User with id=" +
                            userId +
                            " was not found"
            );
        }

        return user;
    }

    private Event getEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Event with id=" +
                                        eventId +
                                        " was not found"
                        )
                );
    }

    private void checkUserExists(Long userId) {
        getUser(userId);
    }

    private void checkEventExists(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException(
                    "Event with id=" +
                            eventId +
                            " was not found"
            );
        }
    }
}