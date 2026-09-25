package com.ticketmgmt.exception;

public class InvalidTicketStatusTransitionException extends RuntimeException {

    public InvalidTicketStatusTransitionException(String currentStatus, String requestedStatus) {
        super(buildMessage(currentStatus, requestedStatus));
    }

    private static String buildMessage(String currentStatus, String requestedStatus) {
        if (currentStatus.equals(requestedStatus)) {
            return "Ticket is already in status " + currentStatus;
        }
        return "Cannot transition ticket from " + currentStatus + " to " + requestedStatus;
    }
}
