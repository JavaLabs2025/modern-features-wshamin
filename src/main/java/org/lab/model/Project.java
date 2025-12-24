package org.lab.model;

import java.util.List;
import java.util.Optional;

public record Project(
        Long id,
        String name,
        User manager,
        Optional<User> teamLead,
        List<User> developers,
        List<User> testers,
        List<Milestone> milestones,
        List<BugReport> bugReports) {
    public Project {
        developers = developers == null ? List.of() : List.copyOf(developers);
        testers = testers == null ? List.of() : List.copyOf(testers);
        milestones = milestones == null ? List.of() : List.copyOf(milestones);
        bugReports = bugReports == null ? List.of() : List.copyOf(bugReports);
    }

    public Project withMilestone(Milestone milestone) {
        return this;
    }
}
