package com.ticketmgmt.config;

import com.ticketmgmt.entity.TicketPriority;
import com.ticketmgmt.entity.TicketStatus;
import com.ticketmgmt.repository.TicketPriorityRepository;
import com.ticketmgmt.repository.TicketStatusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class ReferenceDataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ReferenceDataLoader.class);

    private final TicketStatusRepository ticketStatusRepository;
    private final TicketPriorityRepository ticketPriorityRepository;

    public ReferenceDataLoader(
            TicketStatusRepository ticketStatusRepository,
            TicketPriorityRepository ticketPriorityRepository
    ) {
        this.ticketStatusRepository = ticketStatusRepository;
        this.ticketPriorityRepository = ticketPriorityRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedStatuses();
        seedPriorities();
    }

    private void seedStatuses() {
        if (ticketStatusRepository.count() > 0) {
            return;
        }

        ticketStatusRepository.saveAll(List.of(
                new TicketStatus((short) 1, "OPEN", "Open", (short) 1),
                new TicketStatus((short) 2, "IN_PROGRESS", "In Progress", (short) 2),
                new TicketStatus((short) 3, "RESOLVED", "Resolved", (short) 3),
                new TicketStatus((short) 4, "CLOSED", "Closed", (short) 4),
                new TicketStatus((short) 5, "CANCELLED", "Cancelled", (short) 5)
        ));
        log.info("Seeded ticket status reference data");
    }

    private void seedPriorities() {
        if (ticketPriorityRepository.count() > 0) {
            return;
        }

        ticketPriorityRepository.saveAll(List.of(
                new TicketPriority((short) 1, "LOW", "Low", (short) 1),
                new TicketPriority((short) 2, "MEDIUM", "Medium", (short) 2),
                new TicketPriority((short) 3, "HIGH", "High", (short) 3)
        ));
        log.info("Seeded ticket priority reference data");
    }
}
