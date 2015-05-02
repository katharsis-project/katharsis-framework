package io.katharsis.resource.mock.repository;

import io.katharsis.repository.RelationshipRepository;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class TaskToProjectRepository implements RelationshipRepository<Task, Long, Project, Long> {

    // Used ThreadLocal in case of switching to TestNG and using concurrent tests
    private static final ThreadLocal<Set<Relation<Task>>> repository = new ThreadLocal<Set<Relation<Task>>>() {
        @Override
        protected Set<Relation<Task>> initialValue() {
            return new HashSet<>();
        }
    };

    @Override
    public void addRelation(Task source, Long targetId, String fieldName) {
        repository.get().add(new Relation<>(source, targetId, fieldName));
    }

    @Override
    public void removeRelation(Task source, Long targetId, String fieldName) {
        repository.get().remove(new Relation<>(source, targetId, fieldName));
    }

    @Override
    public Project findOneTarget(Long sourceId, String fieldName) {
        for (Relation<Task> relation : repository.get()) {
            if (relation.getSource().getId().equals(sourceId) &&
                    relation.getFieldName().equals(fieldName)) {
                Project project = new Project();
                project.setId((Long) relation.getTargetId());
                return project;
            }
        }
        return null;
    }

    @Override
    public Iterable<Project> findTargets(Long sourceId, String fieldName) {
        List<Project> projects = new LinkedList<>();
        for (Relation<Task> relation : repository.get()) {
            if (relation.getSource().getId().equals(sourceId) &&
                    relation.getFieldName().equals(fieldName)) {
                Project project = new Project();
                project.setId((Long) relation.getTargetId());
                projects.add(project);
            }
        }
        return projects;
    }
}
