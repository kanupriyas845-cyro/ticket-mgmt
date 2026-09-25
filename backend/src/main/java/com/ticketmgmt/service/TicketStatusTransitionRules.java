package com.ticketmgmt.service;

import com.ticketmgmt.exception.InvalidTicketStatusTransitionException;

import java.util.Map;
import java.util.Set;

/**
 * Enforces allowed ticket status transitions per spec/state-machine.md.
 */
public final class TicketStatusTransitionRules {

    private static final Map<String, Set<String>> ALLOWED_TRANSITIONS = Map.of(
            "OPEN", Set.of("IN_PROGRESS", "CANCELLED"),
            "IN_PROGRESS", Set.of("RESOLVED", "CANCELLED"),
            "RESOLVED", Set.of("CLOSED")
    );

    private TicketStatusTransitionRules() {
    }

    public static void validate(String currentStatus, String requestedStatus) {
        if (currentStatus.equals(requestedStatus)) {
            throw new InvalidTicketStatusTransitionException(currentStatus, requestedStatus);
        }

        Set<String> allowedTargets = ALLOWED_TRANSITIONS.get(currentStatus);
        if (allowedTargets == null || !allowedTargets.contains(requestedStatus)) {
            throw new InvalidTicketStatusTransitionException(currentStatus, requestedStatus);
        }
    }
}
