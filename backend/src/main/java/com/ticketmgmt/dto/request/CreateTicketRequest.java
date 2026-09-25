package com.ticketmgmt.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTicketRequest(
        @NotBlank
        @Size(max = 255)
        String title,
        String description,
        String priority,
        @JsonProperty("status")
        String status,
        @JsonProperty("assigneeId")
        Long assigneeId
) {
}
