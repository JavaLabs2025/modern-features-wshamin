package org.lab.service;

import org.lab.model.BugReport;
import org.lab.model.BugStatus;
import org.lab.model.Project;
import org.lab.model.User;

import java.util.ArrayList;
import java.util.List;

import static org.lab.service.AuthorizationUtils.isDeveloper;
import static org.lab.service.AuthorizationUtils.isTester;

public class BugReportService {
    private final ProjectRepository repository;

    public BugReportService(ProjectRepository repository) {
        this.repository = repository;
    }

    public List<BugReport> getBugReportsForUser(User user) {
        return repository.findAll().stream()
                .filter(p -> p.developers().stream().anyMatch(d -> d.id().equals(user.id())))
                .flatMap(p -> p.bugReports().stream())
                .filter(b -> b.status() == BugStatus.NEW)
                .toList();
    }

    public void createBugReport(Long projectId, String title, String description, User creator) {
        repository.findById(projectId).ifPresent(project -> {
            BugReport report = new BugReport(repository.generateId(), title, description, projectId,
                    BugStatus.NEW);
            List<BugReport> newReports = new ArrayList<>(project.bugReports());
            newReports.add(report);
            repository.save(new Project(project.id(), project.name(), project.manager(), project.teamLead(),
                    project.developers(), project.testers(), project.milestones(), newReports));
        });
    }

    public void updateBugStatus(Long projectId, Long bugId, BugStatus newStatus, User user) {
        repository.findById(projectId).ifPresent(project -> {
            if (isDeveloper(project, user)) {
                if (newStatus != BugStatus.FIXED)
                    throw new IllegalArgumentException("Разработчик может пометить баг только как FIXED");
            } else if (isTester(project, user)) {
                if (newStatus != BugStatus.TESTED && newStatus != BugStatus.CLOSED)
                    throw new IllegalArgumentException("Тестировщик может пометить баг только как TESTED или CLOSED");
            } else {
                throw new IllegalArgumentException("Только разработчик или тестировщик может менять статус бага");
            }

            List<BugReport> newReports = project.bugReports().stream()
                    .map(b -> {
                        if (b.id().equals(bugId)) {
                            return b.withStatus(newStatus);
                        }
                        return b;
                    }).toList();
            repository.save(new Project(project.id(), project.name(), project.manager(), project.teamLead(),
                    project.developers(), project.testers(), project.milestones(), newReports));
        });
    }
}
