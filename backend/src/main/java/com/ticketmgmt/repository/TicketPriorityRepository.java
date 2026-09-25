package com.ticketmgmt.repository;

import com.ticketmgmt.entity.TicketPriority;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TicketPriorityRepository extends JpaRepository<TicketPriority, Short> {

    Optional<TicketPriority> findByCode(String code);
}
