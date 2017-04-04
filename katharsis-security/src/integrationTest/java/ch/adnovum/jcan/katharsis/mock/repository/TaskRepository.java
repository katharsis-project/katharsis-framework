package ch.adnovum.jcan.katharsis.mock.repository;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import ch.adnovum.jcan.katharsis.mock.models.Task;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.QuerySpecResourceRepositoryBase;

@ApplicationScoped
public class TaskRepository extends QuerySpecResourceRepositoryBase<Task, Long> {

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
	public Iterable<Task> findAll(QuerySpec querySpec) {
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
