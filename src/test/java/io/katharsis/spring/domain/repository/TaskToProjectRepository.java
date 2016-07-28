package io.katharsis.spring.domain.repository;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.RelationshipRepository;
import io.katharsis.spring.domain.model.Project;
import io.katharsis.spring.domain.model.Task;
import org.springframework.stereotype.Component;

@Component
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
