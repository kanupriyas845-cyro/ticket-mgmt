package com.ticketmgmt.service;

import com.ticketmgmt.dto.request.CreateCommentRequest;
import com.ticketmgmt.entity.Ticket;
import com.ticketmgmt.entity.TicketComment;
import com.ticketmgmt.entity.TicketPriority;
import com.ticketmgmt.entity.TicketStatus;
import com.ticketmgmt.exception.CommentNotAllowedException;
import com.ticketmgmt.exception.InvalidRequestException;
import com.ticketmgmt.exception.TicketNotFoundException;
import com.ticketmgmt.repository.TicketCommentRepository;
import com.ticketmgmt.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketCommentServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketCommentRepository ticketCommentRepository;

    @InjectMocks
    private TicketCommentService ticketCommentService;

    private Ticket openTicket;
    private Ticket closedTicket;
    private TicketStatus openStatus;
    private TicketStatus closedStatus;
    private TicketPriority mediumPriority;

    @BeforeEach
    void setUp() {
        openStatus = new TicketStatus((short) 1, "OPEN", "Open", (short) 1);
        closedStatus = new TicketStatus((short) 4, "CLOSED", "Closed", (short) 4);
        mediumPriority = new TicketPriority((short) 2, "MEDIUM", "Medium", (short) 2);
        openTicket = new Ticket("Title", "Description", openStatus, mediumPriority);
        closedTicket = new Ticket("Closed ticket", "Description", closedStatus, mediumPriority);
    }

    @Test
    void shouldAddCommentToOpenTicket() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(openTicket));
        when(ticketCommentRepository.save(any(TicketComment.class))).thenAnswer(invocation -> {
            TicketComment comment = invocation.getArgument(0);
            return comment;
        });

        var response = ticketCommentService.addComment(1L, new CreateCommentRequest("Please try again."));

        assertThat(response.body()).isEqualTo("Please try again.");
        assertThat(response.author()).isNull();
        verify(ticketCommentRepository).save(any(TicketComment.class));
    }

    @Test
    void shouldRejectCommentOnClosedTicket() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(closedTicket));

        assertThatThrownBy(() -> ticketCommentService.addComment(1L, new CreateCommentRequest("Too late")))
                .isInstanceOf(CommentNotAllowedException.class)
                .hasMessageContaining("CLOSED");
    }

    @Test
    void shouldThrowNotFound_whenTicketMissingOnAdd() {
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketCommentService.addComment(99L, new CreateCommentRequest("Hello")))
                .isInstanceOf(TicketNotFoundException.class);
    }

    @Test
    void shouldListCommentsForTicket() {
        TicketComment comment = new TicketComment(openTicket, null, "First comment");
        when(ticketRepository.existsById(1L)).thenReturn(true);
        when(ticketCommentRepository.findByTicketId(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(comment)));

        var criteria = ticketCommentService.buildListCriteria(0, 20, "createdAt,asc");
        var response = ticketCommentService.listComments(1L, criteria);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().getFirst().body()).isEqualTo("First comment");
    }

    @Test
    void shouldThrowNotFound_whenTicketMissingOnList() {
        when(ticketRepository.existsById(99L)).thenReturn(false);

        var criteria = ticketCommentService.buildListCriteria(0, 20, "createdAt,asc");

        assertThatThrownBy(() -> ticketCommentService.listComments(99L, criteria))
                .isInstanceOf(TicketNotFoundException.class);
    }

    @Test
    void shouldRejectInvalidPageSize() {
        assertThatThrownBy(() -> ticketCommentService.buildListCriteria(0, 0, "createdAt,asc"))
                .isInstanceOf(InvalidRequestException.class);
    }
}
