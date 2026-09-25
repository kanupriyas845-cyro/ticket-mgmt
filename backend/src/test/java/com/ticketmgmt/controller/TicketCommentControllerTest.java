package com.ticketmgmt.controller;

import com.ticketmgmt.dto.response.CommentResponse;
import com.ticketmgmt.dto.response.PageResponse;
import com.ticketmgmt.exception.CommentNotAllowedException;
import com.ticketmgmt.exception.GlobalExceptionHandler;
import com.ticketmgmt.exception.TicketNotFoundException;
import com.ticketmgmt.service.CommentListCriteria;
import com.ticketmgmt.service.TicketCommentService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TicketCommentController.class)
@Import(GlobalExceptionHandler.class)
class TicketCommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TicketCommentService ticketCommentService;

    @Test
    void shouldAddComment() throws Exception {
        var response = new CommentResponse(
                10L,
                1L,
                "Please try again after clearing cache.",
                null,
                Instant.parse("2026-09-25T11:00:00Z")
        );
        when(ticketCommentService.addComment(eq(1L), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/tickets/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "body": "Please try again after clearing cache."
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/tickets/1/comments/10"))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.ticketId").value(1))
                .andExpect(jsonPath("$.body").value("Please try again after clearing cache."))
                .andExpect(jsonPath("$.author").doesNotExist());
    }

    @Test
    void shouldReturn400_whenBodyBlank() throws Exception {
        mockMvc.perform(post("/api/v1/tickets/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "body": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0].field").value("body"));
    }

    @Test
    void shouldReturn404_whenTicketNotFoundOnAdd() throws Exception {
        when(ticketCommentService.addComment(eq(99L), any()))
                .thenThrow(new TicketNotFoundException(99L));

        mockMvc.perform(post("/api/v1/tickets/99/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "body": "Hello"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ticket not found with id: 99"));
    }

    @Test
    void shouldReturn409_whenTicketClosed() throws Exception {
        when(ticketCommentService.addComment(eq(1L), any()))
                .thenThrow(new CommentNotAllowedException("CLOSED"));

        mockMvc.perform(post("/api/v1/tickets/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "body": "Too late"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Cannot add comment to ticket in CLOSED status"));
    }

    @Test
    void shouldListComments() throws Exception {
        var comment = new CommentResponse(
                10L,
                1L,
                "First comment",
                null,
                Instant.parse("2026-09-25T11:00:00Z")
        );
        var criteria = new CommentListCriteria(0, 20, "createdAt", "asc");
        when(ticketCommentService.buildListCriteria(0, 20, "createdAt,asc")).thenReturn(criteria);
        when(ticketCommentService.listComments(1L, criteria))
                .thenReturn(new PageResponse<>(List.of(comment), 0, 20, 1, 1));

        mockMvc.perform(get("/api/v1/tickets/1/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].body").value("First comment"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldReturn404_whenTicketNotFoundOnList() throws Exception {
        var criteria = new CommentListCriteria(0, 20, "createdAt", "asc");
        when(ticketCommentService.buildListCriteria(0, 20, "createdAt,asc")).thenReturn(criteria);
        when(ticketCommentService.listComments(99L, criteria))
                .thenThrow(new TicketNotFoundException(99L));

        mockMvc.perform(get("/api/v1/tickets/99/comments"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ticket not found with id: 99"));
    }
}
