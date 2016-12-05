package io.katharsis.brave.mock.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import io.katharsis.brave.mock.models.Task;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.repository.ResourceRepositoryBase;
import io.katharsis.resource.list.ResourceList;

public class TaskRepository extends ResourceRepositoryBase<Task, Long> {

	private static final AtomicLong ID_GENERATOR = new AtomicLong(124);

	private static Map<Long, Task> resources = new HashMap<>();

	public TaskRepository() {
		super(Task.class);
	}

	@Override
	public synchronized void delete(Long id) {
		resources.remove(id);
	}

	@Override
	public synchronized <S extends Task> S save(S task) {
		if (task.getName() == null) {
			throw new IllegalStateException("no name available");
		}
		if (task.getId() == null) {
			task.setId(ID_GENERATOR.getAndIncrement());
		}
		resources.put(task.getId(), task);
		return task;
	}

	@Override
	public synchronized ResourceList<Task> findAll(QuerySpec querySpec) {
		return querySpec.apply(resources.values());
	}

	public static void clear() {
		resources.clear();
	}
}
