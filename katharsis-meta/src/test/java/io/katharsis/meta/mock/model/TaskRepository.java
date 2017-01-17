package io.katharsis.meta.mock.model;

import io.katharsis.queryspec.QuerySpec;
import io.katharsis.repository.ResourceRepositoryBase;
import io.katharsis.resource.list.ResourceList;

public class TaskRepository extends ResourceRepositoryBase<Task, Long> {

	public TaskRepository() {
		super(Task.class);
	}

	@Override
	public ResourceList<Task> findAll(QuerySpec querySpec) {
		return null;
	}
}
