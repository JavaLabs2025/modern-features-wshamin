package org.lab.model;

public record BugReport(
        Long id,
        String title,
        String description,
        Long projectId,
        BugStatus status) {
    public BugReport withStatus(BugStatus newStatus) {
        return new BugReport(id, title, description, projectId, newStatus);
    }
}
