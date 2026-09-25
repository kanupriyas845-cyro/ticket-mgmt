package com.ticketmgmt.service;

import com.ticketmgmt.dto.request.CreateTicketRequest;
import com.ticketmgmt.dto.request.UpdateTicketRequest;
import com.ticketmgmt.entity.Ticket;
import com.ticketmgmt.entity.TicketPriority;
import com.ticketmgmt.entity.TicketStatus;
import com.ticketmgmt.exception.InvalidRequestException;
import com.ticketmgmt.exception.TicketEditNotAllowedException;
import com.ticketmgmt.exception.TicketNotFoundException;
import com.ticketmgmt.repository.TicketPriorityRepository;
import com.ticketmgmt.repository.TicketRepository;
import com.ticketmgmt.repository.TicketStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketStatusRepository ticketStatusRepository;

    @Mock
    private TicketPriorityRepository ticketPriorityRepository;

    @InjectMocks
    private TicketService ticketService;

    private TicketStatus openStatus;
    private TicketPriority mediumPriority;
    private TicketPriority highPriority;
    private TicketStatus closedStatus;

    @BeforeEach
    void setUp() {
        openStatus = new TicketStatus((short) 1, "OPEN", "Open", (short) 1);
        closedStatus = new TicketStatus((short) 4, "CLOSED", "Closed", (short) 4);
        mediumPriority = new TicketPriority((short) 2, "MEDIUM", "Medium", (short) 2);
        highPriority = new TicketPriority((short) 3, "HIGH", "High", (short) 3);
    }

    @Test
    void shouldCreateTicketWithDefaultStatusAndPriority() {
        when(ticketStatusRepository.findByCode("OPEN")).thenReturn(Optional.of(openStatus));
        when(ticketPriorityRepository.findByCode("MEDIUM")).thenReturn(Optional.of(mediumPriority));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var request = new CreateTicketRequest("Cannot log in", "Details", null, null, null);
        var response = ticketService.createTicket(request);

        assertThat(response.title()).isEqualTo("Cannot log in");
        assertThat(response.status()).isEqualTo("OPEN");
        assertThat(response.priority()).isEqualTo("MEDIUM");

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(openStatus);
        assertThat(captor.getValue().getPriority()).isEqualTo(mediumPriority);
    }

    @Test
    void shouldCreateTicketWithRequestedPriority() {
        when(ticketStatusRepository.findByCode("OPEN")).thenReturn(Optional.of(openStatus));
        when(ticketPriorityRepository.findByCode("HIGH")).thenReturn(Optional.of(highPriority));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var request = new CreateTicketRequest("Title", "Details", "HIGH", null, null);
        var response = ticketService.createTicket(request);

        assertThat(response.priority()).isEqualTo("HIGH");
    }

    @Test
    void shouldRejectStatusOnCreate() {
        var request = new CreateTicketRequest("Title", "Details", null, "IN_PROGRESS", null);

        assertThatThrownBy(() -> ticketService.createTicket(request))
                .isInstanceOf(InvalidRequestException.class)
                .satisfies(ex -> assertThat(((InvalidRequestException) ex).getFieldErrors())
                        .anyMatch(error -> error.field().equals("status")));
    }

    @Test
    void shouldThrowNotFound_whenTicketMissing() {
        when(ticketRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.getTicket(42L))
                .isInstanceOf(TicketNotFoundException.class)
                .hasMessageContaining("42");
    }

    @Test
    void shouldUpdateTicketTitleAndDescription() {
        Ticket ticket = new Ticket("Old title", "Old description", openStatus, mediumPriority);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        var request = new UpdateTicketRequest("New title", "New description", null, null, null);
        var response = ticketService.updateTicket(1L, request);

        assertThat(response.title()).isEqualTo("New title");
        assertThat(response.description()).isEqualTo("New description");
    }

    @Test
    void shouldRejectUpdate_whenTicketIsClosed() {
        Ticket ticket = new Ticket("Title", "Description", closedStatus, mediumPriority);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        var request = new UpdateTicketRequest("New title", null, null, null, null);

        assertThatThrownBy(() -> ticketService.updateTicket(1L, request))
                .isInstanceOf(TicketEditNotAllowedException.class)
                .hasMessageContaining("CLOSED");
    }

    @Test
    void shouldListTicketsWithSearchAndStatusFilter() {
        Ticket ticket = new Ticket("Login issue", "Cannot log in", openStatus, mediumPriority);
        when(ticketStatusRepository.findByCode("OPEN")).thenReturn(Optional.of(openStatus));
        when(ticketRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(ticket)));

        var criteria = ticketService.buildListCriteria(
                0, 20, "createdAt,desc", "login", List.of("OPEN"), null
        );
        var response = ticketService.listTickets(criteria);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().getFirst().title()).isEqualTo("Login issue");
        assertThat(response.content().getFirst().status()).isEqualTo("OPEN");
    }

    @Test
    void shouldRejectInvalidStatusFilter() {
        when(ticketStatusRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.buildListCriteria(
                0, 20, "createdAt,desc", null, List.of("INVALID"), null
        )).isInstanceOf(InvalidRequestException.class);
    }
}
