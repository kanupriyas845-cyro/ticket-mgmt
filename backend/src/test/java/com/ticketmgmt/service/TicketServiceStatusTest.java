package com.ticketmgmt.service;

import com.ticketmgmt.dto.request.ChangeTicketStatusRequest;
import com.ticketmgmt.entity.Ticket;
import com.ticketmgmt.entity.TicketPriority;
import com.ticketmgmt.entity.TicketStatus;
import com.ticketmgmt.exception.InvalidRequestException;
import com.ticketmgmt.exception.InvalidTicketStatusTransitionException;
import com.ticketmgmt.exception.TicketNotFoundException;
import com.ticketmgmt.repository.TicketPriorityRepository;
import com.ticketmgmt.repository.TicketRepository;
import com.ticketmgmt.repository.TicketStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceStatusTest {

    private static final Set<String> VALID_TRANSITIONS = Set.of(
            "OPEN->IN_PROGRESS",
            "OPEN->CANCELLED",
            "IN_PROGRESS->RESOLVED",
            "IN_PROGRESS->CANCELLED",
            "RESOLVED->CLOSED"
    );

    private static final List<String> ALL_STATUSES = List.of(
            "OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED", "CANCELLED"
    );

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketStatusRepository ticketStatusRepository;

    @Mock
    private TicketPriorityRepository ticketPriorityRepository;

    @InjectMocks
    private TicketService ticketService;

    private final Map<String, TicketStatus> statuses = Map.of(
            "OPEN", new TicketStatus((short) 1, "OPEN", "Open", (short) 1),
            "IN_PROGRESS", new TicketStatus((short) 2, "IN_PROGRESS", "In Progress", (short) 2),
            "RESOLVED", new TicketStatus((short) 3, "RESOLVED", "Resolved", (short) 3),
            "CLOSED", new TicketStatus((short) 4, "CLOSED", "Closed", (short) 4),
            "CANCELLED", new TicketStatus((short) 5, "CANCELLED", "Cancelled", (short) 5)
    );

    private TicketPriority mediumPriority;

    @BeforeEach
    void setUp() {
        mediumPriority = new TicketPriority((short) 2, "MEDIUM", "Medium", (short) 2);
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("allTransitionPairs")
    void shouldChangeStatusForValidTransitionsAndRejectInvalid(
            String from,
            String to,
            boolean valid,
            String expectedMessage
    ) {
        Ticket ticket = new Ticket("Title", "Description", statuses.get(from), mediumPriority);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketStatusRepository.findByCode(to)).thenReturn(Optional.of(statuses.get(to)));

        if (valid) {
            when(ticketRepository.save(ticket)).thenReturn(ticket);

            var response = ticketService.changeTicketStatus(1L, new ChangeTicketStatusRequest(to));

            assertThat(response.status()).isEqualTo(to);
            verify(ticketRepository).save(ticket);
        } else {
            assertThatThrownBy(() -> ticketService.changeTicketStatus(1L, new ChangeTicketStatusRequest(to)))
                    .isInstanceOf(InvalidTicketStatusTransitionException.class)
                    .hasMessage(expectedMessage);
        }
    }

    @Test
    void shouldThrowNotFound_whenTicketMissing() {
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.changeTicketStatus(
                99L, new ChangeTicketStatusRequest("IN_PROGRESS")
        )).isInstanceOf(TicketNotFoundException.class);
    }

    @Test
    void shouldThrowBadRequest_whenStatusCodeUnknown() {
        Ticket ticket = new Ticket("Title", "Description", statuses.get("OPEN"), mediumPriority);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketStatusRepository.findByCode("FOO")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.changeTicketStatus(1L, new ChangeTicketStatusRequest("FOO")))
                .isInstanceOf(InvalidRequestException.class)
                .satisfies(ex -> assertThat(((InvalidRequestException) ex).getFieldErrors())
                        .anyMatch(error -> error.field().equals("status")));
    }

    static Stream<Arguments> allTransitionPairs() {
        return ALL_STATUSES.stream()
                .flatMap(from -> ALL_STATUSES.stream()
                        .map(to -> {
                            boolean valid = VALID_TRANSITIONS.contains(from + "->" + to);
                            String message = from.equals(to)
                                    ? "Ticket is already in status " + from
                                    : "Cannot transition ticket from " + from + " to " + to;
                            return Arguments.of(from, to, valid, message);
                        }));
    }
}
