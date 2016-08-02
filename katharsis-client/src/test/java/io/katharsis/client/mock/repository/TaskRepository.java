package io.katharsis.client.mock.repository;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.annotations.*;
import io.katharsis.resource.exception.ResourceNotFoundException;
import io.katharsis.client.mock.models.Task;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@JsonApiResourceRepository(Task.class)
public class TaskRepository {

	public static final ConcurrentHashMap<Long, Task> map = new ConcurrentHashMap<>();

	@JsonApiSave
	public <S extends Task> S save(S entity) {
		if (entity.getId() == null) {
			entity.setId((long) (map.size() + 1));
		}
		map.put(entity.getId(), entity);

		return entity;
	}

	@JsonApiFindOne
	public Task findOne(Long aLong, QueryParams queryParams) {
		Task task = map.get(aLong);
		if (task == null) {
			throw new ResourceNotFoundException("");
		}
		return task;
	}

	@JsonApiFindAll
	public Iterable<Task> findAll(QueryParams queryParams) {
		return map.values();
	}

	@JsonApiFindAllWithIds
	public Iterable<Task> findAll(Iterable<Long> ids, QueryParams queryParams) {
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

	@JsonApiDelete
	public void delete(Long aLong) {
		map.remove(aLong);
	}
}
