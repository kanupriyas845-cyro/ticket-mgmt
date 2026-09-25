package com.ticketmgmt.dto.response;

import java.time.Instant;

public record CommentResponse(
        Long id,
        Long ticketId,
        String body,
        UserSummaryResponse author,
        Instant createdAt
) {
}
