package org.lab.model;

import java.util.Optional;

public record Ticket(
        Long id,
        String title,
        String description,
        Long projectId,
        Long milestoneId,
        Optional<User> assignee,
        Optional<User> creator,
        TicketStatus status) {
    public Ticket withStatus(TicketStatus newStatus) {
        return new Ticket(id, title, description, projectId, milestoneId, assignee, creator, newStatus);
    }

    public Ticket withAssignee(User newAssignee) {
        return new Ticket(id, title, description, projectId, milestoneId, Optional.ofNullable(newAssignee), creator,
                status);
    }
}
