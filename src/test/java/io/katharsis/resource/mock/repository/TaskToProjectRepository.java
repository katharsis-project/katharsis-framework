package io.katharsis.resource.mock.repository;

import io.katharsis.repository.RelationshipRepository;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;

public class TaskToProjectRepository implements RelationshipRepository<Task, Long, Project, Long> {
    @Override
    public void addRelation(Task source, Project target) {

    }

    @Override
    public void removeRelation(Task source, Project target) {

    }

    @Override
    public Project findOneTarget(Long sourceId) {
        return null;
    }

    @Override
    public Iterable<Project> findTarget(Long sourceId) {
        return null;
    }

    @Override
    public Long findOneTargetId(Long sourceId) {
        return null;
    }

    @Override
    public Iterable<Long> findTargetIds(Long sourceId) {
        return null;
    }
}
