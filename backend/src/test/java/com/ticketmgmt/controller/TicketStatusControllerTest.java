package com.ticketmgmt.controller;

import com.ticketmgmt.dto.request.ChangeTicketStatusRequest;
import com.ticketmgmt.dto.response.TicketResponse;
import com.ticketmgmt.exception.GlobalExceptionHandler;
import com.ticketmgmt.exception.InvalidRequestException;
import com.ticketmgmt.exception.InvalidTicketStatusTransitionException;
import com.ticketmgmt.exception.TicketNotFoundException;
import com.ticketmgmt.service.TicketService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TicketController.class)
@Import(GlobalExceptionHandler.class)
class TicketStatusControllerTest {

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

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TicketService ticketService;

    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("allTransitionPairs")
    void shouldReturnCorrectStatusForAllTransitions(
            String from,
            String to,
            boolean valid,
            String expectedMessage
    ) throws Exception {
        if (valid) {
            var response = ticketResponse(to);
            when(ticketService.changeTicketStatus(eq(1L), any(ChangeTicketStatusRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(patch("/api/v1/tickets/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\":\"" + to + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(to));
        } else {
            when(ticketService.changeTicketStatus(eq(1L), any(ChangeTicketStatusRequest.class)))
                    .thenThrow(new InvalidTicketStatusTransitionException(from, to));

            mockMvc.perform(patch("/api/v1/tickets/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\":\"" + to + "\"}"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.error").value("Conflict"))
                    .andExpect(jsonPath("$.message").value(expectedMessage));
        }
    }

    @Test
    void shouldReturn404_whenTicketNotFound() throws Exception {
        when(ticketService.changeTicketStatus(eq(99L), any()))
                .thenThrow(new TicketNotFoundException(99L));

        mockMvc.perform(patch("/api/v1/tickets/99/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ticket not found with id: 99"));
    }

    @Test
    void shouldReturn400_whenStatusMissing() throws Exception {
        mockMvc.perform(patch("/api/v1/tickets/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("status"));
    }

    @Test
    void shouldReturn400_whenStatusCodeUnknown() throws Exception {
        when(ticketService.changeTicketStatus(eq(1L), any()))
                .thenThrow(new InvalidRequestException("status", "must be a valid status code"));

        mockMvc.perform(patch("/api/v1/tickets/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"FOO\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("status"));
    }

    private static TicketResponse ticketResponse(String status) {
        return new TicketResponse(
                1L,
                "Title",
                "Description",
                status,
                "MEDIUM",
                null,
                Instant.parse("2026-09-25T10:00:00Z"),
                Instant.parse("2026-09-25T10:00:00Z")
        );
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
