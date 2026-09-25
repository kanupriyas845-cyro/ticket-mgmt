package com.ticketmgmt.controller;

import com.ticketmgmt.dto.response.PageResponse;
import com.ticketmgmt.dto.response.TicketResponse;
import com.ticketmgmt.dto.response.TicketSummaryResponse;
import com.ticketmgmt.exception.GlobalExceptionHandler;
import com.ticketmgmt.exception.InvalidRequestException;
import com.ticketmgmt.exception.TicketNotFoundException;
import com.ticketmgmt.service.TicketListCriteria;
import com.ticketmgmt.service.TicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TicketController.class)
@Import(GlobalExceptionHandler.class)
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TicketService ticketService;

    @Test
    void shouldCreateTicket() throws Exception {
        var response = new TicketResponse(
                1L,
                "Cannot log in",
                "Details",
                "OPEN",
                "MEDIUM",
                null,
                Instant.parse("2026-09-25T10:00:00Z"),
                Instant.parse("2026-09-25T10:00:00Z")
        );
        when(ticketService.createTicket(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Cannot log in",
                                  "description": "Details"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/tickets/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"));
    }

    @Test
    void shouldReturnValidationError_whenTitleMissing() throws Exception {
        mockMvc.perform(post("/api/v1/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "Details"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0].field").value("title"));
    }

    @Test
    void shouldReturnTicketById() throws Exception {
        var response = new TicketResponse(
                1L,
                "Cannot log in",
                "Details",
                "OPEN",
                "MEDIUM",
                null,
                Instant.parse("2026-09-25T10:00:00Z"),
                Instant.parse("2026-09-25T10:00:00Z")
        );
        when(ticketService.getTicket(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/tickets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Cannot log in"));
    }

    @Test
    void shouldReturn404_whenTicketNotFound() throws Exception {
        when(ticketService.getTicket(99L)).thenThrow(new TicketNotFoundException(99L));

        mockMvc.perform(get("/api/v1/tickets/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ticket not found with id: 99"));
    }

    @Test
    void shouldListTicketsWithFilters() throws Exception {
        var summary = new TicketSummaryResponse(
                1L,
                "Login issue",
                "OPEN",
                "MEDIUM",
                null,
                Instant.parse("2026-09-25T10:00:00Z"),
                Instant.parse("2026-09-25T10:00:00Z")
        );
        var criteria = new TicketListCriteria(0, 20, "createdAt", "desc", "login", List.of("OPEN"), null);
        when(ticketService.buildListCriteria(0, 20, "createdAt,desc", "login", List.of("OPEN"), null))
                .thenReturn(criteria);
        when(ticketService.listTickets(criteria))
                .thenReturn(new PageResponse<>(List.of(summary), 0, 20, 1, 1));

        mockMvc.perform(get("/api/v1/tickets")
                        .param("search", "login")
                        .param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Login issue"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldUpdateTicket() throws Exception {
        var response = new TicketResponse(
                1L,
                "Updated title",
                "Updated description",
                "OPEN",
                "HIGH",
                null,
                Instant.parse("2026-09-25T10:00:00Z"),
                Instant.parse("2026-09-25T11:00:00Z")
        );
        when(ticketService.updateTicket(eq(1L), any())).thenReturn(response);

        mockMvc.perform(patch("/api/v1/tickets/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Updated title",
                                  "priority": "HIGH"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated title"))
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    @Test
    void shouldReturn400_whenStatusSentOnUpdate() throws Exception {
        when(ticketService.updateTicket(eq(1L), any()))
                .thenThrow(new InvalidRequestException("status", "must not be set on this endpoint"));

        mockMvc.perform(patch("/api/v1/tickets/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Updated title",
                                  "status": "IN_PROGRESS"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("status"));
    }
}
