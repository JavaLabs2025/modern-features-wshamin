package org.lab.service;

import org.lab.model.Project;
import org.lab.model.Role;
import org.lab.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.lab.service.AuthorizationUtils.*;

public class ProjectService {
    private final ProjectRepository repository;

    public ProjectService(ProjectRepository repository) {
        this.repository = repository;
    }

    public Project createProject(String name, User creator) {
        long id = repository.generateId();
        Project project = new Project(id, name, creator, Optional.empty(), List.of(), List.of(), List.of(), List.of());
        return repository.save(project);
    }

    public Project getProject(Long id) {
        return repository.findById(id).orElse(null);
    }

    public List<Project> getProjectsForUser(User user) {
        return repository.findAll().stream()
                .filter(p -> p.manager().id().equals(user.id()) ||
                        (p.teamLead().isPresent() && p.teamLead().get().id().equals(user.id())) ||
                        p.developers().stream().anyMatch(d -> d.id().equals(user.id())) ||
                        p.testers().stream().anyMatch(t -> t.id().equals(user.id())))
                .toList();
    }

    public void addDeveloper(Long projectId, User developer, User assigner) {
        repository.findById(projectId).ifPresent(project -> {
            if (!isManager(project, assigner)) {
                throw new IllegalArgumentException("Только менеджер может добавлять разработчиков");
            }
            List<User> newDevs = new ArrayList<>(project.developers());
            newDevs.add(developer);
            repository.save(new Project(project.id(), project.name(), project.manager(), project.teamLead(), newDevs,
                    project.testers(), project.milestones(), project.bugReports()));
        });
    }

    public void addTester(Long projectId, User tester, User assigner) {
        repository.findById(projectId).ifPresent(project -> {
            if (!isManager(project, assigner)) {
                throw new IllegalArgumentException("Только менеджер может добавлять тестировщиков");
            }
            List<User> newTesters = new ArrayList<>(project.testers());
            newTesters.add(tester);
            repository.save(new Project(project.id(), project.name(), project.manager(), project.teamLead(),
                    project.developers(), newTesters, project.milestones(), project.bugReports()));
        });
    }

    public void setTeamLead(Long projectId, User teamLead, User assigner) {
        repository.findById(projectId).ifPresent(project -> {
            if (!isManager(project, assigner)) {
                throw new IllegalArgumentException("Только менеджер может назначать team lead");
            }
            repository.save(new Project(project.id(), project.name(), project.manager(), Optional.of(teamLead),
                    project.developers(), project.testers(), project.milestones(), project.bugReports()));
        });
    }

    public Role getUserRole(Long projectId, Long userId) {
        Project p = repository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Проект не найден"));

        if (p.manager().id().equals(userId))
            return new Role.Manager();
        if (p.teamLead().isPresent() && p.teamLead().get().id().equals(userId))
            return new Role.TeamLead();
        if (p.developers().stream().anyMatch(u -> u.id().equals(userId)))
            return new Role.Developer();
        if (p.testers().stream().anyMatch(u -> u.id().equals(userId)))
            return new Role.Tester();

        throw new IllegalArgumentException("У пользователя нет роли в этом проекте");
    }

    public String getRoleDescription(Long projectId, Long userId) {
        Role role = getUserRole(projectId, userId);
        return switch (role) {
            case Role.Manager _ -> "Менеджер: Может управлять всем проектом";
            case Role.TeamLead _ -> "Team Lead: Может управлять тикетами";
            case Role.Developer _ -> "Разработчик: Может выполнять тикеты и управлять багами";
            case Role.Tester _ -> "Тестировщик: Может управлять багами";
        };
    }
}
