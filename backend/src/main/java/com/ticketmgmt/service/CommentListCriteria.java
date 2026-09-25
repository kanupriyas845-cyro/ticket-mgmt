package com.ticketmgmt.service;

public record CommentListCriteria(
        int page,
        int size,
        String sortProperty,
        String sortDirection
) {
}
