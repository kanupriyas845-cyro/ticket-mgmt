package com.ticketmgmt.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ticketmgmt.validation.AtLeastOneFieldPresent;
import jakarta.validation.constraints.Size;

@AtLeastOneFieldPresent(fields = {"title", "description", "priority"})
public record UpdateTicketRequest(
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
