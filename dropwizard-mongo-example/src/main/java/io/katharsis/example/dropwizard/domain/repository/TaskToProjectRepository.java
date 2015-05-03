package io.katharsis.example.dropwizard.domain.repository;

import io.katharsis.example.dropwizard.domain.model.Project;
import io.katharsis.example.dropwizard.domain.model.Task;
import io.katharsis.repository.RelationshipRepository;

public class TaskToProjectRepository implements RelationshipRepository<Task, Long, Project, Long> {

    public void addRelation(Task task, Long projectId, String fieldName) {

    }

    public void removeRelation(Task task, Long projectId, String fieldName) {

    }

    public Project findOneTarget(Long sourceId, String fieldName) {
        return null;
    }

    public Iterable<Project> findTargets(Long sourceId, String fieldName) {
        return null;
    }
}
