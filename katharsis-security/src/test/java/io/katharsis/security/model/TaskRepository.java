package io.katharsis.security.model;

import java.util.HashMap;
import java.util.Map;

import io.katharsis.queryspec.QuerySpec;
import io.katharsis.repository.ResourceRepositoryBase;
import io.katharsis.resource.list.ResourceList;

public class TaskRepository extends ResourceRepositoryBase<Task, Long> {

	private static final Map<Long, Task> TASKS = new HashMap<>();

	public TaskRepository() {
		super(Task.class);
	}

	@Override
	public <S extends Task> S save(S entity) {
		TASKS.put(entity.getId(), entity);
		return entity;
	}

	@Override
	public ResourceList<Task> findAll(QuerySpec querySpec) {
		return querySpec.apply(TASKS.values());
	}

	@Override
	public void delete(Long id) {
		TASKS.remove(id);
	}

	public static void clear() {
		TASKS.clear();
	}
}
