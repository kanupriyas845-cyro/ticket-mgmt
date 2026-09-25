package com.ticketmgmt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ticket_status")
public class TicketStatus {

    @Id
    private Short id;

    @Column(nullable = false, unique = true, length = 32)
    private String code;

    @Column(nullable = false, length = 64)
    private String label;

    @Column(name = "sort_order", nullable = false)
    private short sortOrder;

    protected TicketStatus() {
    }

    public TicketStatus(Short id, String code, String label, short sortOrder) {
        this.id = id;
        this.code = code;
        this.label = label;
        this.sortOrder = sortOrder;
    }

    public Short getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public short getSortOrder() {
        return sortOrder;
    }
}
