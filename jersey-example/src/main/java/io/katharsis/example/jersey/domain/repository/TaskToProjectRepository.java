package io.katharsis.example.jersey.domain.repository;

import io.katharsis.example.jersey.domain.model.Project;
import io.katharsis.example.jersey.domain.model.Task;
import io.katharsis.repository.RelationshipRepository;

public class TaskToProjectRepository implements RelationshipRepository<Task, Long, Project, Long> {

    @Override
    public void setRelation(Task task, Long projectId, String fieldName) {

    }

    @Override
    public void setRelations(Task task, Iterable<Long> projectId, String fieldName) {

    }

    @Override
    public void addRelations(Task task, Iterable<Long> projectIds, String fieldName) {

    }

    @Override
    public void removeRelations(Task task, Iterable<Long> projectIds, String fieldName) {

    }

    @Override
    public Project findOneTarget(Long sourceId, String fieldName) {
        return null;
    }

    @Override
    public Iterable<Project> findManyTargets(Long sourceId, String fieldName) {
        return null;
    }
}
