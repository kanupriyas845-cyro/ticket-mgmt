package com.ticketmgmt.dto.response;

import java.time.Instant;

public record TicketResponse(
        Long id,
        String title,
        String description,
        String status,
        String priority,
        UserSummaryResponse assignee,
        Instant createdAt,
        Instant updatedAt
) {
}
