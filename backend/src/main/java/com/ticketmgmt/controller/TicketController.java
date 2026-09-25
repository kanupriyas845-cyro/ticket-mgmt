package com.ticketmgmt.controller;

import com.ticketmgmt.dto.request.ChangeTicketStatusRequest;
import com.ticketmgmt.dto.request.CreateTicketRequest;
import com.ticketmgmt.dto.request.UpdateTicketRequest;
import com.ticketmgmt.dto.response.PageResponse;
import com.ticketmgmt.dto.response.TicketResponse;
import com.ticketmgmt.dto.response.TicketSummaryResponse;
import com.ticketmgmt.service.TicketListCriteria;
import com.ticketmgmt.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        TicketResponse response = ticketService.createTicket(request);
        URI location = URI.create("/api/v1/tickets/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public PageResponse<TicketSummaryResponse> listTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<String> status,
            @RequestParam(required = false) String priority
    ) {
        TicketListCriteria criteria = ticketService.buildListCriteria(page, size, sort, search, status, priority);
        return ticketService.listTickets(criteria);
    }

    @GetMapping("/{ticketId}")
    public TicketResponse getTicket(@PathVariable Long ticketId) {
        return ticketService.getTicket(ticketId);
    }

    @PatchMapping("/{ticketId}")
    public TicketResponse updateTicket(
            @PathVariable Long ticketId,
            @Valid @RequestBody UpdateTicketRequest request
    ) {
        return ticketService.updateTicket(ticketId, request);
    }

    @PatchMapping("/{ticketId}/status")
    public TicketResponse changeTicketStatus(
            @PathVariable Long ticketId,
            @Valid @RequestBody ChangeTicketStatusRequest request
    ) {
        return ticketService.changeTicketStatus(ticketId, request);
    }
}
