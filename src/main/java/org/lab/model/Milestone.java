package org.lab.model;

import java.time.LocalDate;
import java.util.List;

public record Milestone(
        Long id,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        Long projectId,
        MilestoneStatus status,
        List<Ticket> tickets) {
    public Milestone {
        tickets = tickets == null ? List.of() : List.copyOf(tickets);
    }
}
