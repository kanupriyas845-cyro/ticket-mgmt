package com.ticketmgmt.integration;

import com.jayway.jsonpath.JsonPath;
import com.ticketmgmt.entity.Ticket;
import com.ticketmgmt.repository.TicketPriorityRepository;
import com.ticketmgmt.repository.TicketRepository;
import com.ticketmgmt.repository.TicketStatusRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full-stack verification: real service, real DB, real HTTP — no mocked business logic.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TicketStatusTransitionIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketStatusRepository ticketStatusRepository;

    @Autowired
    private TicketPriorityRepository ticketPriorityRepository;

    @ParameterizedTest(name = "reject {0} -> OPEN via API")
    @CsvSource({
            "CLOSED",
            "RESOLVED",
            "CANCELLED"
    })
    void shouldRejectReopenTransitions(String fromStatus) throws Exception {
        Ticket ticket = saveTicket(fromStatus);

        mockMvc.perform(patch("/api/v1/tickets/{id}/status", ticket.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"OPEN\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("Cannot transition ticket from " + fromStatus + " to OPEN"));
    }

    @ParameterizedTest(name = "allow {0} -> {1} via API")
    @CsvSource({
            "OPEN, IN_PROGRESS",
            "OPEN, CANCELLED",
            "IN_PROGRESS, RESOLVED",
            "IN_PROGRESS, CANCELLED",
            "RESOLVED, CLOSED"
    })
    void shouldAllowValidTransitions(String fromStatus, String toStatus) throws Exception {
        Ticket ticket = saveTicket(fromStatus);

        mockMvc.perform(patch("/api/v1/tickets/{id}/status", ticket.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"" + toStatus + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(toStatus));
    }

    @ParameterizedTest(name = "reject {0} -> {1} via API")
    @CsvSource({
            "CLOSED, IN_PROGRESS",
            "CLOSED, RESOLVED",
            "CLOSED, CANCELLED",
            "CANCELLED, IN_PROGRESS",
            "CANCELLED, RESOLVED",
            "CANCELLED, CLOSED",
            "OPEN, RESOLVED",
            "OPEN, CLOSED",
            "IN_PROGRESS, CLOSED",
            "IN_PROGRESS, OPEN",
            "RESOLVED, IN_PROGRESS",
            "RESOLVED, CANCELLED",
            "OPEN, OPEN",
            "IN_PROGRESS, IN_PROGRESS",
            "RESOLVED, RESOLVED"
    })
    void shouldRejectInvalidTransitions(String fromStatus, String toStatus) throws Exception {
        Ticket ticket = saveTicket(fromStatus);

        String expectedMessage = fromStatus.equals(toStatus)
                ? "Ticket is already in status " + fromStatus
                : "Cannot transition ticket from " + fromStatus + " to " + toStatus;

        mockMvc.perform(patch("/api/v1/tickets/{id}/status", ticket.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"" + toStatus + "\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(expectedMessage));
    }

    @Test
    void shouldWalkFullHappyPathViaApi() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/v1/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Integration test\",\"description\":\"Path test\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andReturn();

        long ticketId = JsonPath.parse(createResult.getResponse().getContentAsString())
                .read("$.id", Long.class);

        mockMvc.perform(patch("/api/v1/tickets/{id}/status", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        mockMvc.perform(patch("/api/v1/tickets/{id}/status", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"RESOLVED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"));

        mockMvc.perform(patch("/api/v1/tickets/{id}/status", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CLOSED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));

        mockMvc.perform(patch("/api/v1/tickets/{id}/status", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"OPEN\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Cannot transition ticket from CLOSED to OPEN"));
    }

    private Ticket saveTicket(String statusCode) {
        var status = ticketStatusRepository.findByCode(statusCode).orElseThrow();
        var priority = ticketPriorityRepository.findByCode("MEDIUM").orElseThrow();
        return ticketRepository.save(new Ticket("Test ticket", "Description", status, priority));
    }
}
