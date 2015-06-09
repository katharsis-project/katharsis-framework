package io.katharsis.resource.mock.repository;

import io.katharsis.repository.RelationshipRepository;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.User;
import io.katharsis.resource.mock.repository.util.Relation;

import java.util.*;

public class UserToProjectRepository implements RelationshipRepository<User, Long, Project, Long> {

    // Used ThreadLocal in case of switching to TestNG and using concurrent tests
    private static final ThreadLocal<Set<Relation<User>>> THREAD_LOCAL_REPOSITORY = new ThreadLocal<Set<Relation<User>>>() {
        @Override
        protected Set<Relation<User>> initialValue() {
            return new HashSet<>();
        }
    };

    @Override
    public void setRelation(User source, Long targetId, String fieldName) {
        removeRelations(fieldName);
        if (targetId != null) {
            THREAD_LOCAL_REPOSITORY.get().add(new Relation<>(source, targetId, fieldName));
        }
    }

    @Override
    public void setRelations(User source, Iterable<Long> targetIds, String fieldName) {
        removeRelations(fieldName);
        if (targetIds != null) {
            for (Long targetId : targetIds) {
                THREAD_LOCAL_REPOSITORY.get().add(new Relation<>(source, targetId, fieldName));
            }
        }
    }

    @Override
    public void addRelations(User source, Iterable<Long> targetIds, String fieldName) {
        targetIds.forEach(targetId ->
                        THREAD_LOCAL_REPOSITORY.get().add(new Relation<>(source, targetId, fieldName))
        );
    }

    @Override
    public void removeRelations(User source, Iterable<Long> targetIds, String fieldName) {
        targetIds.forEach(targetId -> {
            Iterator<Relation<User>> iterator = THREAD_LOCAL_REPOSITORY.get().iterator();
            while (iterator.hasNext()) {
                Relation<User> next = iterator.next();
                if (next.getFieldName().equals(fieldName) && next.getTargetId().equals(targetId)) {
                    iterator.remove();
                }
            }
        });
    }

    private void removeRelations(String fieldName) {
        Iterator<Relation<User>> iterator = THREAD_LOCAL_REPOSITORY.get().iterator();
        while (iterator.hasNext()) {
            Relation<User> next = iterator.next();
            if (next.getFieldName().equals(fieldName)) {
                iterator.remove();
            }
        }
    }

    @Override
    public Project findOneTarget(Long sourceId, String fieldName) {
        Set<Relation<User>> relations = THREAD_LOCAL_REPOSITORY.get();
        for (Relation<User> relation : relations) {
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
    public Iterable<Project> findManyTargets(Long sourceId, String fieldName) {
        List<Project> projects = new LinkedList<>();
        for (Relation<User> relation : THREAD_LOCAL_REPOSITORY.get()) {
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
