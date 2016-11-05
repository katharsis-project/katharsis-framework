package io.katharsis.resource.mock.repository;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.QuerySpecBulkRelationshipRepository;
import io.katharsis.repository.annotations.JsonApiFindManyTargets;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.models.User;
import io.katharsis.resource.mock.repository.util.Relation;

public class UserToTaskRepository implements QuerySpecBulkRelationshipRepository<User, Long, Task, Long> {

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
	public Map<Long, Task> findOneTargets(Iterable<Long> sourceIds, String fieldName, QuerySpec querySpec) {
		Map<Long, Task> map = new HashMap<>();
		for (Long sourceId : sourceIds) {
			map.put(sourceId, findOneTarget(sourceId, fieldName, querySpec));
		}
		return map;
	}

	@Override
	public Map<Long, Iterable<Task>> findManyTargets(Iterable<Long> sourceIds, String fieldName, QuerySpec querySpec) {
		Map<Long, Iterable<Task>> map = new HashMap<>();
		for (Long sourceId : sourceIds) {
			map.put(sourceId, findManyTargets(sourceId, fieldName, querySpec));
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
	public Iterable<Task> findManyTargets(Long sourceId, String fieldName, QuerySpec querySpec) {
		List<Task> projects = new LinkedList<>();
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
