package io.katharsis.rs.resource.repository;

import io.katharsis.legacy.queryParams.QueryParams;
import io.katharsis.legacy.repository.RelationshipRepository;
import io.katharsis.rs.resource.model.Project;
import io.katharsis.rs.resource.model.Task;

public class TaskToProjectRepository implements RelationshipRepository<Task, Long, Project, Long> {

    @Override
    public void setRelation(Task task, Long projectId, String fieldName) {

    }

    @Override
    public void setRelations(Task task, Iterable<Long> projectId, String fieldName) {

    }

    @Override
    public void addRelations(Task source, Iterable<Long> targetIds, String fieldName) {
    }

    @Override
    public void removeRelations(Task source, Iterable<Long> targetIds, String fieldName) {
    }

    @Override
    public Project findOneTarget(Long sourceId, String fieldName, QueryParams requestParams) {
        return null;
    }

    @Override
    public Iterable<Project> findManyTargets(Long sourceId, String fieldName, QueryParams requestParams) {
        return null;
    }
}
