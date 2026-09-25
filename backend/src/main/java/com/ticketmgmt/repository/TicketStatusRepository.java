package com.ticketmgmt.repository;

import com.ticketmgmt.entity.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TicketStatusRepository extends JpaRepository<TicketStatus, Short> {

    Optional<TicketStatus> findByCode(String code);
}
