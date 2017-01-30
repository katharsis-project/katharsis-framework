package io.katharsis.resource.mock.repository;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.katharsis.core.internal.utils.MultivaluedMap;
import io.katharsis.legacy.repository.annotations.JsonApiFindManyTargets;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.repository.BulkRelationshipRepositoryV2;
import io.katharsis.resource.list.DefaultResourceList;
import io.katharsis.resource.list.ResourceList;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.models.User;
import io.katharsis.resource.mock.repository.util.Relation;

public class UserToTaskRepository implements BulkRelationshipRepositoryV2<User, Long, Task, Long> {

	private static final ConcurrentMap<Relation<User>, Integer> THREAD_LOCAL_REPOSITORY = new ConcurrentHashMap<>();

	@Override
	public void setRelation(User source, Long targetId, String fieldName) {
		removeRelations(fieldName);
		if (targetId != null) {
			THREAD_LOCAL_REPOSITORY.put(new Relation<>(source, targetId, fieldName), 0);
		}
	}

	@Override
	public void setRelations(User source, Iterable<Long> targetIds, String fieldName) {
		removeRelations(fieldName);
		if (targetIds != null) {
			for (Long targetId : targetIds) {
				THREAD_LOCAL_REPOSITORY.put(new Relation<>(source, targetId, fieldName), 0);
			}
		}
	}

	@Override
	public void addRelations(User source, Iterable<Long> targetIds, String fieldName) {
		for (Long targetId : targetIds) {
			THREAD_LOCAL_REPOSITORY.put(new Relation<>(source, targetId, fieldName), 0);
		}
	}

	@Override
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

	@Override
	public MultivaluedMap<Long, Task> findTargets(Iterable<Long> sourceIds, String fieldName, QuerySpec querySpec) {
		MultivaluedMap<Long, Task> map = new MultivaluedMap<>();
		for (Long sourceId : sourceIds) {
			map.addAll(sourceId, findManyTargets(sourceId, fieldName, querySpec));
		}
		return map;
	}

	@Override
	public Task findOneTarget(Long sourceId, String fieldName, QuerySpec querySpec) {
		for (Relation<User> relation : THREAD_LOCAL_REPOSITORY.keySet()) {
			if (relation.getSource().getId().equals(sourceId) && relation.getFieldName().equals(fieldName)) {
				Task project = new Task();
				project.setId((Long) relation.getTargetId());
				return project;
			}
		}
		return null;
	}

	@JsonApiFindManyTargets
	public ResourceList<Task> findManyTargets(Long sourceId, String fieldName, QuerySpec querySpec) {
		ResourceList<Task> projects = new DefaultResourceList<>();
		for (Relation<User> relation : THREAD_LOCAL_REPOSITORY.keySet()) {
			if (relation.getSource().getId().equals(sourceId) && relation.getFieldName().equals(fieldName)) {
				Task project = new Task();
				project.setId((Long) relation.getTargetId());
				projects.add(project);
			}
		}
		return projects;
	}

	public static void clear() {
		THREAD_LOCAL_REPOSITORY.clear();
	}

	@Override
	public Class<User> getSourceResourceClass() {
		return User.class;
	}

	@Override
	public Class<Task> getTargetResourceClass() {
		return Task.class;
	}

}
