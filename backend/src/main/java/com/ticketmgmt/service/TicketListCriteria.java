package com.ticketmgmt.service;

import java.util.List;

public record TicketListCriteria(
        int page,
        int size,
        String sortProperty,
        String sortDirection,
        String search,
        List<String> statusCodes,
        String priorityCode
) {
}
