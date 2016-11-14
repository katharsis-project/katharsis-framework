package io.katharsis.brave.mock.repository;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import io.katharsis.brave.mock.models.Task;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.QuerySpecResourceRepository;
import io.katharsis.resource.exception.ResourceNotFoundException;

public class TaskRepository implements QuerySpecResourceRepository<Task, Long> {

	public static final ConcurrentHashMap<Long, Task> map = new ConcurrentHashMap<>();

	@Override
	public <S extends Task> S save(S entity) {
		if (entity.getId() == null) {
			entity.setId((long) (map.size() + 1));
		}
		map.put(entity.getId(), entity);

		return entity;
	}

	@Override
	public Task findOne(Long aLong, QuerySpec querySpec) {
		Task task = map.get(aLong);
		if (task == null) {
			throw new ResourceNotFoundException("");
		}
		return task;
	}

	@Override
	public Iterable<Task> findAll(QuerySpec querySpec) {
		return map.values();
	}

	@Override
	public Iterable<Task> findAll(Iterable<Long> ids, QuerySpec querySpec) {
		List<Task> values = new LinkedList<>();
		for (Task value : map.values()) {
			if (contains(value, ids)) {
				values.add(value);
			}
		}
		return values;
	}

	private boolean contains(Task value, Iterable<Long> ids) {
		for (Long id : ids) {
			if (value.getId().equals(id)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void delete(Long aLong) {
		map.remove(aLong);
	}

	@Override
	public Class<Task> getResourceClass() {
		return Task.class;
	}
}
