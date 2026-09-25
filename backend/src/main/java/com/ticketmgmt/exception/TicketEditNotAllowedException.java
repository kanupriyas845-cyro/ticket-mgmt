package com.ticketmgmt.exception;

public class TicketEditNotAllowedException extends RuntimeException {

    public TicketEditNotAllowedException(String statusCode) {
        super("Cannot update ticket in " + statusCode + " status");
    }
}
