package com.ticketmgmt.repository;

import com.ticketmgmt.entity.Ticket;
import com.ticketmgmt.entity.TicketPriority;
import com.ticketmgmt.entity.TicketStatus;
import com.ticketmgmt.service.TicketListCriteria;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class TicketSpecifications {

    private TicketSpecifications() {
    }

    public static Specification<Ticket> withCriteria(TicketListCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            if (Long.class != query.getResultType() && long.class != query.getResultType()) {
                root.fetch("status", JoinType.INNER);
                root.fetch("priority", JoinType.INNER);
                root.fetch("assignee", JoinType.LEFT);
                query.distinct(true);
            }

            List<Predicate> predicates = new ArrayList<>();

            if (criteria.search() != null && !criteria.search().isBlank()) {
                String pattern = "%" + criteria.search().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern)
                ));
            }

            if (criteria.statusCodes() != null && !criteria.statusCodes().isEmpty()) {
                Join<Ticket, TicketStatus> statusJoin = root.join("status");
                predicates.add(statusJoin.get("code").in(criteria.statusCodes()));
            }

            if (criteria.priorityCode() != null && !criteria.priorityCode().isBlank()) {
                Join<Ticket, TicketPriority> priorityJoin = root.join("priority");
                predicates.add(criteriaBuilder.equal(priorityJoin.get("code"), criteria.priorityCode()));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
