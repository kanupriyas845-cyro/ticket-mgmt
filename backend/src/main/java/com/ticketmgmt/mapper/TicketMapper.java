package com.ticketmgmt.mapper;

import com.ticketmgmt.dto.response.TicketResponse;
import com.ticketmgmt.dto.response.TicketSummaryResponse;
import com.ticketmgmt.dto.response.UserSummaryResponse;
import com.ticketmgmt.entity.Ticket;
import com.ticketmgmt.entity.User;

public final class TicketMapper {

    private TicketMapper() {
    }

    public static TicketResponse toResponse(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus().getCode(),
                ticket.getPriority().getCode(),
                toAssigneeSummary(ticket.getAssignee()),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt()
        );
    }

    public static TicketSummaryResponse toSummary(Ticket ticket) {
        return new TicketSummaryResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getStatus().getCode(),
                ticket.getPriority().getCode(),
                toAssigneeSummary(ticket.getAssignee()),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt()
        );
    }

    private static UserSummaryResponse toAssigneeSummary(User assignee) {
        if (assignee == null) {
            return null;
        }
        return new UserSummaryResponse(assignee.getId(), assignee.getDisplayName());
    }
}
