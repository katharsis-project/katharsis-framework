package io.katharsis.resource.mock.repository;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.annotations.*;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.User;
import io.katharsis.resource.mock.repository.util.Relation;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@JsonApiRelationshipRepository(source = User.class, target = Project.class)
public class UserToProjectRepository {

    private static final ConcurrentMap<Relation<User>, Integer> THREAD_LOCAL_REPOSITORY = new ConcurrentHashMap<>();

    @JsonApiSetRelation
    public void setRelation(User source, Long targetId, String fieldName) {
        removeRelations(fieldName);
        if (targetId != null) {
            THREAD_LOCAL_REPOSITORY.put(new Relation<>(source, targetId, fieldName), 0);
        }
    }

    @JsonApiSetRelations
    public void setRelations(User source, Iterable<Long> targetIds, String fieldName) {
        removeRelations(fieldName);
        if (targetIds != null) {
            for (Long targetId : targetIds) {
                THREAD_LOCAL_REPOSITORY.put(new Relation<>(source, targetId, fieldName), 0);
            }
        }
    }

    @JsonApiAddRelations
    public void addRelations(User source, Iterable<Long> targetIds, String fieldName) {
        targetIds.forEach(targetId ->
                THREAD_LOCAL_REPOSITORY.put(new Relation<>(source, targetId, fieldName), 0)
        );
    }

    @JsonApiRemoveRelations
    public void removeRelations(User source, Iterable<Long> targetIds, String fieldName) {
        targetIds.forEach(targetId -> {
            Iterator<Relation<User>> iterator = THREAD_LOCAL_REPOSITORY.keySet().iterator();
            while (iterator.hasNext()) {
                Relation<User> next = iterator.next();
                if (next.getFieldName().equals(fieldName) && next.getTargetId().equals(targetId)) {
                    iterator.remove();
                }
            }
        });
    }

    public void removeRelations(String fieldName) {
        Iterator<Relation<User>> iterator = THREAD_LOCAL_REPOSITORY.keySet().iterator();
        while (iterator.hasNext()) {
            Relation<User> next = iterator.next();
            if (next.getFieldName().equals(fieldName)) {
                iterator.remove();
            }
        }
    }

    @JsonApiFindOneTarget
    public Project findOneTarget(Long sourceId, String fieldName, QueryParams queryParams) {
        for (Relation<User> relation : THREAD_LOCAL_REPOSITORY.keySet()) {
            if (relation.getSource().getId().equals(sourceId) &&
                relation.getFieldName().equals(fieldName)) {
                Project project = new Project();
                project.setId((Long) relation.getTargetId());
                return project;
            }
        }
        return null;
    }

    @JsonApiFindManyTargets
    public Iterable<Project> findManyTargets(Long sourceId, String fieldName,  QueryParams queryParams) {
        List<Project> projects = new LinkedList<>();
        THREAD_LOCAL_REPOSITORY.keySet()
            .stream()
            .filter(relation -> relation.getSource().getId().equals(sourceId) && relation.getFieldName().equals
                (fieldName)).forEach(relation -> {
            Project project = new Project();
            project.setId((Long) relation.getTargetId());
            projects.add(project);
        });
        return projects;
    }
}
