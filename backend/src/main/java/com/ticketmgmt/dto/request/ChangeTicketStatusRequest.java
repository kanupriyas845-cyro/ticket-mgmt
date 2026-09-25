package com.ticketmgmt.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ChangeTicketStatusRequest(
        @NotBlank
        String status
) {
}
