package org.lab.service;

import org.lab.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.lab.service.AuthorizationUtils.*;

public class TicketService {
    private final ProjectRepository repository;

    public TicketService(ProjectRepository repository) {
        this.repository = repository;
    }

    public List<Ticket> getTicketsForUser(User user) {
        return repository.findAll().stream()
                .flatMap(p -> p.milestones().stream())
                .flatMap(m -> m.tickets().stream())
                .filter(t -> t.assignee().isPresent() && t.assignee().get().id().equals(user.id()))
                .toList();
    }

    public List<Ticket> getTicketsCreatedByUser(User user) {
        return repository.findAll().stream()
                .flatMap(p -> p.milestones().stream())
                .flatMap(m -> m.tickets().stream())
                .filter(t -> t.creator().isPresent() && t.creator().get().id().equals(user.id()))
                .toList();
    }

    public void createTicket(Long projectId, Long milestoneId, String title, String description, User creator) {
        repository.findById(projectId).ifPresent(project -> {
            boolean isAuthorized = isManager(project, creator) || isTeamLead(project, creator);
            if (!isAuthorized) {
                throw new IllegalArgumentException("Только менеджер или team lead могут создавать тикеты");
            }

            List<Milestone> newMilestones = project.milestones().stream()
                    .map(m -> {
                        if (m.id().equals(milestoneId)) {
                            Ticket ticket = new Ticket(repository.generateId(), title, description, projectId,
                                    milestoneId, Optional.empty(), Optional.of(creator), TicketStatus.NEW);
                            List<Ticket> newTickets = new ArrayList<>(m.tickets());
                            newTickets.add(ticket);
                            return new Milestone(m.id(), m.name(), m.startDate(), m.endDate(), m.projectId(),
                                    m.status(), newTickets);
                        }
                        return m;
                    }).toList();
            repository.save(new Project(project.id(), project.name(), project.manager(), project.teamLead(),
                    project.developers(), project.testers(), newMilestones, project.bugReports()));
        });
    }

    public void assignTicket(Long projectId, Long milestoneId, Long ticketId, User developer, User assigner) {
        repository.findById(projectId).ifPresent(project -> {
            boolean isAuthorized = isManager(project, assigner) || isTeamLead(project, assigner);
            if (!isAuthorized) {
                throw new IllegalArgumentException("Только менеджер или team lead могут назначить разработчика");
            }

            List<Milestone> newMilestones = project.milestones().stream()
                    .map(m -> {
                        if (m.id().equals(milestoneId)) {
                            List<Ticket> newTickets = m.tickets().stream()
                                    .map(t -> {
                                        if (t.id().equals(ticketId)) {
                                            return t.withAssignee(developer).withStatus(TicketStatus.ACCEPTED);
                                        }
                                        return t;
                                    }).toList();
                            return new Milestone(m.id(), m.name(), m.startDate(), m.endDate(), m.projectId(),
                                    m.status(), newTickets);
                        }
                        return m;
                    }).toList();
            repository.save(new Project(project.id(), project.name(), project.manager(), project.teamLead(),
                    project.developers(), project.testers(), newMilestones, project.bugReports()));
        });
    }

    public void updateTicketStatus(Long projectId, Long milestoneId, Long ticketId, TicketStatus newStatus, User user) {
        repository.findById(projectId).ifPresent(project -> {
            if (!isDeveloper(project, user) && !isManager(project, user) && !isTeamLead(project, user)) {
                throw new IllegalArgumentException("Пользователь не имеет права менять статус тикета");
            }

            List<Milestone> newMilestones = project.milestones().stream()
                    .map(m -> {
                        if (m.id().equals(milestoneId)) {
                            List<Ticket> newTickets = m.tickets().stream()
                                    .map(t -> {
                                        if (t.id().equals(ticketId)) {
                                            return t.withStatus(newStatus);
                                        }
                                        return t;
                                    }).toList();
                            return new Milestone(m.id(), m.name(), m.startDate(), m.endDate(), m.projectId(),
                                    m.status(), newTickets);
                        }
                        return m;
                    }).toList();
            repository.save(new Project(project.id(), project.name(), project.manager(), project.teamLead(),
                    project.developers(), project.testers(), newMilestones, project.bugReports()));
        });
    }
}
