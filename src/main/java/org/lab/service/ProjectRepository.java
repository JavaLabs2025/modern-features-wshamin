package org.lab.service;

import org.lab.model.Project;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class ProjectRepository {
    private final Map<Long, Project> projects = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public Project save(Project project) {
        projects.put(project.id(), project);
        return project;
    }

    public Optional<Project> findById(Long id) {
        return Optional.ofNullable(projects.get(id));
    }

    public long generateId() {
        return idGenerator.getAndIncrement();
    }

    public Collection<Project> findAll() {
        return projects.values();
    }
}
