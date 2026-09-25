package com.ticketmgmt.exception;

import java.time.Instant;
import java.util.List;

/**
 * Standard API error body per spec/02-conventions.md and spec/api-contract.md.
 */
public record ApiErrorResponse(
        int status,
        String error,
        String message,
        Instant timestamp,
        List<FieldError> errors
) {
    public record FieldError(String field, String message) {
    }

    public static ApiErrorResponse of(int status, String error, String message) {
        return new ApiErrorResponse(status, error, message, Instant.now(), null);
    }

    public static ApiErrorResponse validation(int status, String error, String message, List<FieldError> errors) {
        return new ApiErrorResponse(status, error, message, Instant.now(), errors);
    }
}
