package ru.practicum.ewm.controller.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.practicum.ewm.model.CommentStatus;
import ru.practicum.ewm.repository.CommentRepository;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class InternalCommentControllerTest {

    @Mock
    private CommentRepository commentRepository;

    private InternalCommentController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        controller =
                new InternalCommentController(
                        commentRepository
                );
    }

    @Test
    void countPublishedComments_shouldReturnRepositoryCount() {
        when(
                commentRepository.countByEventIdAndStatus(
                        10L,
                        CommentStatus.PUBLISHED
                )
        ).thenReturn(3L);

        long result =
                controller.countPublishedComments(10L);

        assertThat(result).isEqualTo(3L);

        verify(commentRepository)
                .countByEventIdAndStatus(
                        10L,
                        CommentStatus.PUBLISHED
                );
    }

    @Test
    void countPublishedComments_shouldReturnCountsForEvents() {
        List<Long> eventIds =
                List.of(10L, 20L);

        when(
                commentRepository.countByEventIdsAndStatus(
                        eventIds,
                        CommentStatus.PUBLISHED
                )
        ).thenReturn(
                List.of(
                        new Object[]{10L, 2L},
                        new Object[]{20L, 5L}
                )
        );

        Map<Long, Long> result =
                controller.countPublishedComments(eventIds);

        assertThat(result)
                .containsExactlyInAnyOrderEntriesOf(
                        Map.of(
                                10L, 2L,
                                20L, 5L
                        )
                );
    }

    @Test
    void countPublishedComments_shouldReturnEmptyMapForEmptyList() {
        Map<Long, Long> result =
                controller.countPublishedComments(List.of());

        assertThat(result).isEmpty();

        verifyNoInteractions(commentRepository);
    }
}