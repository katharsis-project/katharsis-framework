package io.katharsis.resource.mock.repository;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.katharsis.legacy.queryParams.QueryParams;
import io.katharsis.legacy.repository.RelationshipRepository;
import io.katharsis.resource.mock.models.ProjectPolymorphic;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.repository.util.Relation;

public class ProjectPolymorphicToObjectRepository extends AbstractRelationShipRepository<ProjectPolymorphic> implements RelationshipRepository<ProjectPolymorphic, Long, Object, Long> {

    private final static ConcurrentMap<Relation<ProjectPolymorphic>, Integer> STATIC_REPOSITORY = new ConcurrentHashMap<>();

    @Override
    ConcurrentMap<Relation<ProjectPolymorphic>, Integer> getRepo() {
        return STATIC_REPOSITORY;
    }

    @Override
    public void setRelation(ProjectPolymorphic source, Long targetId, String fieldName) {
        super.setRelation(source, targetId, fieldName);
    }

    @Override
    public void setRelations(ProjectPolymorphic source, Iterable<Long> targetIds, String fieldName) {
        super.setRelations(source, targetIds, fieldName);
    }

    @Override
    public void addRelations(ProjectPolymorphic source, Iterable<Long> targetIds, String fieldName) {
        super.addRelations(source, targetIds, fieldName);
    }

    @Override
    public void removeRelations(ProjectPolymorphic source, Iterable<Long> targetIds, String fieldName) {
        super.removeRelations(source, targetIds, fieldName);
    }

    @Override
    public Object findOneTarget(Long sourceId, String fieldName, QueryParams queryParams) {
        for (Relation<ProjectPolymorphic> relation : getRepo().keySet()) {
            if (relation.getSource().getId().equals(sourceId) &&
                    relation.getFieldName().equals(fieldName)) {
                Task task = new Task();
                task.setId((Long) relation.getTargetId());
                return task;
            }
        }
        return null;
    }

    @Override
    public Iterable<Object> findManyTargets(Long sourceId, String fieldName, QueryParams queryParams) {
        List<Object> tasks = new LinkedList<>();
        for (Relation<ProjectPolymorphic> relation : getRepo().keySet()) {
            if (relation.getSource().getId().equals(sourceId) && relation.getFieldName().equals(fieldName)) {
                Task task = new Task();
                task.setId((Long) relation.getTargetId());
                tasks.add(task);
            }
        }
        return tasks;
    }

}
