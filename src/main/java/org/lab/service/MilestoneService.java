package org.lab.service;

import org.lab.model.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.lab.service.AuthorizationUtils.isManager;

public class MilestoneService {
    private final ProjectRepository repository;

    public MilestoneService(ProjectRepository repository) {
        this.repository = repository;
    }

    public void createMilestone(Long projectId, String name, LocalDate start, LocalDate end, User creator) {
        repository.findById(projectId).ifPresent(project -> {
            if (!isManager(project, creator)) {
                throw new IllegalArgumentException("Только менеджер может создавать milestone");
            }
            Milestone milestone = new Milestone(repository.generateId(), name, start, end, projectId,
                    MilestoneStatus.OPEN, List.of());
            List<Milestone> newMilestones = new ArrayList<>(project.milestones());
            newMilestones.add(milestone);
            repository.save(new Project(project.id(), project.name(), project.manager(), project.teamLead(),
                    project.developers(), project.testers(), newMilestones, project.bugReports()));
        });
    }

    public void updateMilestoneStatus(Long projectId, Long milestoneId, MilestoneStatus newStatus, User user) {
        repository.findById(projectId).ifPresent(project -> {
            if (!isManager(project, user)) {
                throw new IllegalArgumentException("Только менеджер может менять статус milestone");
            }

            if (newStatus == MilestoneStatus.ACTIVE) {
                boolean hasActive = project.milestones().stream()
                        .anyMatch(m -> m.status() == MilestoneStatus.ACTIVE && !m.id().equals(milestoneId));
                if (hasActive) {
                    throw new IllegalStateException(
                            "Проект уже имеет активный milestone. Закройте или деактивируйте его.");
                }
            }

            List<Milestone> newMilestones = project.milestones().stream()
                    .map(m -> {
                        if (m.id().equals(milestoneId)) {
                            if (newStatus == MilestoneStatus.CLOSED) {
                                boolean allTicketsCompleted = m.tickets().stream()
                                        .allMatch(t -> t.status() == TicketStatus.COMPLETED);
                                if (!allTicketsCompleted) {
                                    throw new IllegalStateException(
                                            "Нельзя закрыть milestone с незавершенными тикетами");
                                }
                            }
                            return new Milestone(m.id(), m.name(), m.startDate(), m.endDate(), m.projectId(), newStatus,
                                    m.tickets());
                        }
                        return m;
                    }).toList();
            repository.save(new Project(project.id(), project.name(), project.manager(), project.teamLead(),
                    project.developers(), project.testers(), newMilestones, project.bugReports()));
        });
    }
}
