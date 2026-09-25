package com.ticketmgmt.repository;

import com.ticketmgmt.entity.TicketComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketCommentRepository extends JpaRepository<TicketComment, Long> {

    @EntityGraph(attributePaths = {"ticket", "author"})
    Page<TicketComment> findByTicketId(Long ticketId, Pageable pageable);
}
