package com.ticketmgmt.mapper;

import com.ticketmgmt.dto.response.CommentResponse;
import com.ticketmgmt.dto.response.UserSummaryResponse;
import com.ticketmgmt.entity.TicketComment;
import com.ticketmgmt.entity.User;

public final class CommentMapper {

    private CommentMapper() {
    }

    public static CommentResponse toResponse(TicketComment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getTicket().getId(),
                comment.getBody(),
                toAuthorSummary(comment.getAuthor()),
                comment.getCreatedAt()
        );
    }

    private static UserSummaryResponse toAuthorSummary(User author) {
        if (author == null) {
            return null;
        }
        return new UserSummaryResponse(author.getId(), author.getDisplayName());
    }
}
