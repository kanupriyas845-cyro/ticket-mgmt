package com.ticketmgmt.dto.response;

import java.time.Instant;

public record TicketSummaryResponse(
        Long id,
        String title,
        String status,
        String priority,
        UserSummaryResponse assignee,
        Instant createdAt,
        Instant updatedAt
) {
}
