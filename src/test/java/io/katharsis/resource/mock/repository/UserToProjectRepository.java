package io.katharsis.resource.mock.repository;

import io.katharsis.repository.RelationshipRepository;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.User;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class UserToProjectRepository implements RelationshipRepository<User, Long, Project, Long> {

    // Used ThreadLocal in case of switching to TestNG and using concurrent tests
    private static final ThreadLocal<Set<Relation<User>>> repository = new ThreadLocal<Set<Relation<User>>>() {
        @Override
        protected Set<Relation<User>> initialValue() {
            return new HashSet<>();
        }
    };

    @Override
    public void addRelation(User source, Long targetId, String fieldName) {
        repository.get().add(new Relation<>(source, targetId, fieldName));
    }

    @Override
    public void removeRelation(User source, Long targetId, String fieldName) {
        repository.get().remove(new Relation<>(source, targetId, fieldName));
    }

    @Override
    public Project findOneTarget(Long sourceId, String fieldName) {
        for (Relation<User> relation : repository.get()) {
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
        for (Relation<User> relation : repository.get()) {
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
