package com.ticketmgmt.service;

import com.ticketmgmt.dto.request.ChangeTicketStatusRequest;
import com.ticketmgmt.dto.request.CreateTicketRequest;
import com.ticketmgmt.dto.request.UpdateTicketRequest;
import com.ticketmgmt.dto.response.PageResponse;
import com.ticketmgmt.dto.response.TicketResponse;
import com.ticketmgmt.dto.response.TicketSummaryResponse;
import com.ticketmgmt.entity.Ticket;
import com.ticketmgmt.entity.TicketPriority;
import com.ticketmgmt.entity.TicketStatus;
import com.ticketmgmt.exception.InvalidRequestException;
import com.ticketmgmt.exception.TicketEditNotAllowedException;
import com.ticketmgmt.exception.TicketNotFoundException;
import com.ticketmgmt.mapper.TicketMapper;
import com.ticketmgmt.repository.TicketPriorityRepository;
import com.ticketmgmt.repository.TicketRepository;
import com.ticketmgmt.repository.TicketSpecifications;
import com.ticketmgmt.repository.TicketStatusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketService.class);

    static final String DEFAULT_STATUS_CODE = "OPEN";
    static final String DEFAULT_PRIORITY_CODE = "MEDIUM";
    static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of("createdAt", "updatedAt");
    private static final Set<String> NON_EDITABLE_STATUS_CODES = Set.of("CLOSED", "CANCELLED");

    private final TicketRepository ticketRepository;
    private final TicketStatusRepository ticketStatusRepository;
    private final TicketPriorityRepository ticketPriorityRepository;

    public TicketService(
            TicketRepository ticketRepository,
            TicketStatusRepository ticketStatusRepository,
            TicketPriorityRepository ticketPriorityRepository
    ) {
        this.ticketRepository = ticketRepository;
        this.ticketStatusRepository = ticketStatusRepository;
        this.ticketPriorityRepository = ticketPriorityRepository;
    }

    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request) {
        rejectStatusField(request.status());
        rejectAssigneeField(request.assigneeId());

        TicketStatus status = requireStatus(DEFAULT_STATUS_CODE);
        TicketPriority priority = resolvePriority(request.priority());

        Ticket ticket = new Ticket(request.title(), request.description(), status, priority);
        Ticket saved = ticketRepository.save(ticket);
        log.info("Created ticket id={}", saved.getId());
        return TicketMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public TicketResponse getTicket(Long ticketId) {
        Ticket ticket = findTicketOrThrow(ticketId);
        return TicketMapper.toResponse(ticket);
    }

    @Transactional
    public TicketResponse updateTicket(Long ticketId, UpdateTicketRequest request) {
        rejectStatusField(request.status());
        rejectAssigneeField(request.assigneeId());

        Ticket ticket = findTicketOrThrow(ticketId);
        assertTicketEditable(ticket);

        if (request.title() != null) {
            if (request.title().isBlank()) {
                throw new InvalidRequestException("title", "must not be blank");
            }
            ticket.setTitle(request.title());
        }

        if (request.description() != null) {
            ticket.setDescription(request.description());
        }

        if (request.priority() != null) {
            ticket.setPriority(resolvePriority(request.priority()));
        }

        Ticket saved = ticketRepository.save(ticket);
        log.info("Updated ticket id={}", saved.getId());
        return TicketMapper.toResponse(saved);
    }

    @Transactional
    public TicketResponse changeTicketStatus(Long ticketId, ChangeTicketStatusRequest request) {
        Ticket ticket = findTicketOrThrow(ticketId);
        String currentStatus = ticket.getStatus().getCode();

        TicketStatus newStatus = ticketStatusRepository.findByCode(request.status())
                .orElseThrow(() -> new InvalidRequestException("status", "must be a valid status code"));

        TicketStatusTransitionRules.validate(currentStatus, newStatus.getCode());

        ticket.setStatus(newStatus);
        Ticket saved = ticketRepository.save(ticket);
        log.info("Changed ticket id={} status from {} to {}", saved.getId(), currentStatus, newStatus.getCode());
        return TicketMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<TicketSummaryResponse> listTickets(TicketListCriteria criteria) {
        validateListCriteria(criteria);

        Pageable pageable = PageRequest.of(
                criteria.page(),
                criteria.size(),
                Sort.by(Sort.Direction.fromString(criteria.sortDirection()), criteria.sortProperty())
        );

        Specification<Ticket> specification = TicketSpecifications.withCriteria(criteria);
        Page<Ticket> page = ticketRepository.findAll(specification, pageable);
        return PageResponse.from(page.map(TicketMapper::toSummary));
    }

    public TicketListCriteria buildListCriteria(
            int page,
            int size,
            String sort,
            String search,
            List<String> statusParams,
            String priority
    ) {
        validatePagination(page, size);
        String[] sortParts = parseSort(sort);
        List<String> statusCodes = parseStatusCodes(statusParams);
        validateStatusCodes(statusCodes);
        validatePriorityCode(priority);

        return new TicketListCriteria(
                page,
                size,
                sortParts[0],
                sortParts[1],
                search,
                statusCodes,
                priority
        );
    }

    private Ticket findTicketOrThrow(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));
    }

    private TicketStatus requireStatus(String code) {
        return ticketStatusRepository.findByCode(code)
                .orElseThrow(() -> new IllegalStateException("Reference status not found: " + code));
    }

    private TicketPriority resolvePriority(String priorityCode) {
        String code = priorityCode == null || priorityCode.isBlank()
                ? DEFAULT_PRIORITY_CODE
                : priorityCode;

        return ticketPriorityRepository.findByCode(code)
                .orElseThrow(() -> new InvalidRequestException("priority", "must be a valid priority code"));
    }

    private void rejectStatusField(String status) {
        if (status != null) {
            throw new InvalidRequestException("status", "must not be set on this endpoint");
        }
    }

    private void rejectAssigneeField(Long assigneeId) {
        if (assigneeId != null) {
            throw new InvalidRequestException("assigneeId", "is not supported yet");
        }
    }

    private void assertTicketEditable(Ticket ticket) {
        if (NON_EDITABLE_STATUS_CODES.contains(ticket.getStatus().getCode())) {
            throw new TicketEditNotAllowedException(ticket.getStatus().getCode());
        }
    }

    private void validateListCriteria(TicketListCriteria criteria) {
        validatePagination(criteria.page(), criteria.size());
        validateSort(criteria.sortProperty(), criteria.sortDirection());
        validateStatusCodes(criteria.statusCodes());
        validatePriorityCode(criteria.priorityCode());
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
            throw new InvalidRequestException("sort", "must be one of: createdAt,desc or updatedAt,desc");
        }
        if (!"asc".equalsIgnoreCase(direction) && !"desc".equalsIgnoreCase(direction)) {
            throw new InvalidRequestException("sort", "direction must be asc or desc");
        }
    }

    private void validateStatusCodes(List<String> statusCodes) {
        if (statusCodes == null || statusCodes.isEmpty()) {
            return;
        }
        for (String code : statusCodes) {
            if (ticketStatusRepository.findByCode(code).isEmpty()) {
                throw new InvalidRequestException("status", "must be a valid status code");
            }
        }
    }

    private void validatePriorityCode(String priorityCode) {
        if (priorityCode == null || priorityCode.isBlank()) {
            return;
        }
        if (ticketPriorityRepository.findByCode(priorityCode).isEmpty()) {
            throw new InvalidRequestException("priority", "must be a valid priority code");
        }
    }

    private String[] parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return new String[]{"createdAt", "desc"};
        }

        String[] parts = sort.split(",", 2);
        if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
            throw new InvalidRequestException("sort", "must be in the form property,direction");
        }
        return new String[]{parts[0].trim(), parts[1].trim()};
    }

    private List<String> parseStatusCodes(List<String> statusParams) {
        if (statusParams == null || statusParams.isEmpty()) {
            return List.of();
        }

        List<String> codes = new ArrayList<>();
        for (String param : statusParams) {
            if (param == null || param.isBlank()) {
                continue;
            }
            for (String code : param.split(",")) {
                String trimmed = code.trim();
                if (!trimmed.isEmpty()) {
                    codes.add(trimmed);
                }
            }
        }
        return codes;
    }
}
