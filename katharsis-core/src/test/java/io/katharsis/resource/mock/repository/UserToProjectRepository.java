package io.katharsis.resource.mock.repository;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.katharsis.legacy.repository.annotations.JsonApiAddRelations;
import io.katharsis.legacy.repository.annotations.JsonApiFindManyTargets;
import io.katharsis.legacy.repository.annotations.JsonApiFindOneTarget;
import io.katharsis.legacy.repository.annotations.JsonApiRelationshipRepository;
import io.katharsis.legacy.repository.annotations.JsonApiRemoveRelations;
import io.katharsis.legacy.repository.annotations.JsonApiSetRelation;
import io.katharsis.legacy.repository.annotations.JsonApiSetRelations;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.User;
import io.katharsis.resource.mock.repository.util.Relation;

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
        for (Long targetId : targetIds) {
            THREAD_LOCAL_REPOSITORY.put(new Relation<>(source, targetId, fieldName), 0);
        }
    }

    @JsonApiRemoveRelations
    public void removeRelations(User source, Iterable<Long> targetIds, String fieldName) {
        for (Long targetId : targetIds) {
            Iterator<Relation<User>> iterator = THREAD_LOCAL_REPOSITORY.keySet().iterator();
            while (iterator.hasNext()) {
                Relation<User> next = iterator.next();
                if (next.getFieldName().equals(fieldName) && next.getTargetId().equals(targetId)) {
                    iterator.remove();
                }
            }
        }
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
    public Project findOneTarget(Long sourceId, String fieldName, QuerySpec querySpec) {
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
    public Iterable<Project> findManyTargets(Long sourceId, String fieldName, QuerySpec querySpec) {
        List<Project> projects = new LinkedList<>();
        for (Relation<User> relation: THREAD_LOCAL_REPOSITORY.keySet()) {
            if (relation.getSource().getId().equals(sourceId) && relation.getFieldName().equals(fieldName)) {
                Project project = new Project();
                project.setId((Long) relation.getTargetId());
                projects.add(project);
            }
        }
        return projects;
    }

	public static void clear() {
		THREAD_LOCAL_REPOSITORY.clear();		
	}
}
