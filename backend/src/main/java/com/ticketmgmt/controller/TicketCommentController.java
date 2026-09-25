package com.ticketmgmt.controller;

import com.ticketmgmt.dto.request.CreateCommentRequest;
import com.ticketmgmt.dto.response.CommentResponse;
import com.ticketmgmt.dto.response.PageResponse;
import com.ticketmgmt.service.CommentListCriteria;
import com.ticketmgmt.service.TicketCommentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/tickets/{ticketId}/comments")
public class TicketCommentController {

    private final TicketCommentService ticketCommentService;

    public TicketCommentController(TicketCommentService ticketCommentService) {
        this.ticketCommentService = ticketCommentService;
    }

    @GetMapping
    public PageResponse<CommentResponse> listComments(
            @PathVariable Long ticketId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,asc") String sort
    ) {
        CommentListCriteria criteria = ticketCommentService.buildListCriteria(page, size, sort);
        return ticketCommentService.listComments(ticketId, criteria);
    }

    @PostMapping
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long ticketId,
            @Valid @RequestBody CreateCommentRequest request
    ) {
        CommentResponse response = ticketCommentService.addComment(ticketId, request);
        URI location = URI.create("/api/v1/tickets/" + ticketId + "/comments/" + response.id());
        return ResponseEntity.created(location).body(response);
    }
}
