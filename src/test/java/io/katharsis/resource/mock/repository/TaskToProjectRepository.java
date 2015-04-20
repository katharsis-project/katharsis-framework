package io.katharsis.resource.mock.repository;

import io.katharsis.repository.RelationshipRepository;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;

public class TaskToProjectRepository implements RelationshipRepository<Task, Long, Project> {
    @Override
    public void addRelation(Task source, Project target, String fieldName) {

    }

    @Override
    public void removeRelation(Task source, Project target, String fieldName) {

    }

    @Override
    public Project findOneTarget(Long sourceId, String fieldName) {
        return new Project();
    }

    @Override
    public Iterable<Project> findTargets(Long sourceId, String fieldName) {
        return null;
    }
}
