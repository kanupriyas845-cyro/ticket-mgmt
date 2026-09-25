package com.ticketmgmt.service;

import com.ticketmgmt.dto.request.CreateCommentRequest;
import com.ticketmgmt.dto.response.CommentResponse;
import com.ticketmgmt.dto.response.PageResponse;
import com.ticketmgmt.entity.Ticket;
import com.ticketmgmt.entity.TicketComment;
import com.ticketmgmt.exception.CommentNotAllowedException;
import com.ticketmgmt.exception.InvalidRequestException;
import com.ticketmgmt.exception.TicketNotFoundException;
import com.ticketmgmt.mapper.CommentMapper;
import com.ticketmgmt.repository.TicketCommentRepository;
import com.ticketmgmt.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class TicketCommentService {

    private static final Logger log = LoggerFactory.getLogger(TicketCommentService.class);

    static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> NON_COMMENTABLE_STATUS_CODES = Set.of("CLOSED", "CANCELLED");
    private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of("createdAt");

    private final TicketRepository ticketRepository;
    private final TicketCommentRepository ticketCommentRepository;

    public TicketCommentService(
            TicketRepository ticketRepository,
            TicketCommentRepository ticketCommentRepository
    ) {
        this.ticketRepository = ticketRepository;
        this.ticketCommentRepository = ticketCommentRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<CommentResponse> listComments(Long ticketId, CommentListCriteria criteria) {
        assertTicketExists(ticketId);
        validateListCriteria(criteria);

        Pageable pageable = PageRequest.of(
                criteria.page(),
                criteria.size(),
                Sort.by(Sort.Direction.fromString(criteria.sortDirection()), criteria.sortProperty())
        );

        Page<TicketComment> page = ticketCommentRepository.findByTicketId(ticketId, pageable);
        return PageResponse.from(page.map(CommentMapper::toResponse));
    }

    @Transactional
    public CommentResponse addComment(Long ticketId, CreateCommentRequest request) {
        Ticket ticket = findTicketOrThrow(ticketId);
        assertCommentAllowed(ticket);

        TicketComment comment = new TicketComment(ticket, null, request.body());
        TicketComment saved = ticketCommentRepository.save(comment);
        log.info("Added comment id={} to ticket id={}", saved.getId(), ticketId);
        return CommentMapper.toResponse(saved);
    }

    public CommentListCriteria buildListCriteria(int page, int size, String sort) {
        validatePagination(page, size);
        String[] sortParts = parseSort(sort);
        validateSort(sortParts[0], sortParts[1]);

        return new CommentListCriteria(page, size, sortParts[0], sortParts[1]);
    }

    private void assertTicketExists(Long ticketId) {
        if (!ticketRepository.existsById(ticketId)) {
            throw new TicketNotFoundException(ticketId);
        }
    }

    private Ticket findTicketOrThrow(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));
    }

    private void assertCommentAllowed(Ticket ticket) {
        if (NON_COMMENTABLE_STATUS_CODES.contains(ticket.getStatus().getCode())) {
            throw new CommentNotAllowedException(ticket.getStatus().getCode());
        }
    }

    private void validateListCriteria(CommentListCriteria criteria) {
        validatePagination(criteria.page(), criteria.size());
        validateSort(criteria.sortProperty(), criteria.sortDirection());
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new InvalidRequestException("page", "must be greater than or equal to 0");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new InvalidRequestException("size", "must be between 1 and " + MAX_PAGE_SIZE);
        }
    }

    private void validateSort(String property, String direction) {
        if (!ALLOWED_SORT_PROPERTIES.contains(property)) {
            throw new InvalidRequestException("sort", "must be createdAt,asc or createdAt,desc");
        }
        if (!"asc".equalsIgnoreCase(direction) && !"desc".equalsIgnoreCase(direction)) {
            throw new InvalidRequestException("sort", "direction must be asc or desc");
        }
    }

    private String[] parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return new String[]{"createdAt", "asc"};
        }

        String[] parts = sort.split(",", 2);
        if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
            throw new InvalidRequestException("sort", "must be in the form property,direction");
        }
        return new String[]{parts[0].trim(), parts[1].trim()};
    }
}
