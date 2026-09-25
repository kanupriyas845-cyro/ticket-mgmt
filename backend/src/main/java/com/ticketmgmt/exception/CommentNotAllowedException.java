package com.ticketmgmt.exception;

public class CommentNotAllowedException extends RuntimeException {

    public CommentNotAllowedException(String statusCode) {
        super("Cannot add comment to ticket in " + statusCode + " status");
    }
}
