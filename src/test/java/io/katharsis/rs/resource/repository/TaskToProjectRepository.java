package io.katharsis.rs.resource.repository;

import io.katharsis.repository.RelationshipRepository;
import io.katharsis.rs.resource.model.Project;
import io.katharsis.rs.resource.model.Task;

public class TaskToProjectRepository implements RelationshipRepository<Task, Long, Project, Long> {

    @Override
    public void addRelation(Task task, Long projectId, String fieldName) {

    }

    @Override
    public void removeRelation(Task task, Long projectId, String fieldName) {

    }

    @Override
    public Project findOneTarget(Long sourceId, String fieldName) {
        return null;
    }

    @Override
    public Iterable<Project> findTargets(Long sourceId, String fieldName) {
        return null;
    }
}
