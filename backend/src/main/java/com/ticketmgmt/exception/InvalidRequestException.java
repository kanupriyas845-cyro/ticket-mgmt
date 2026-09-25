package com.ticketmgmt.exception;

import java.util.List;

public class InvalidRequestException extends RuntimeException {

    private final List<ApiErrorResponse.FieldError> fieldErrors;

    public InvalidRequestException(String message) {
        super(message);
        this.fieldErrors = null;
    }

    public InvalidRequestException(String field, String message) {
        super("Validation failed");
        this.fieldErrors = List.of(new ApiErrorResponse.FieldError(field, message));
    }

    public List<ApiErrorResponse.FieldError> getFieldErrors() {
        return fieldErrors;
    }

    public boolean hasFieldErrors() {
        return fieldErrors != null && !fieldErrors.isEmpty();
    }
}
