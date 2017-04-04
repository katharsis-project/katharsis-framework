package io.katharsis.client.mock.repository;

import java.util.concurrent.ConcurrentHashMap;

import io.katharsis.client.mock.models.TaskSubType;
import io.katharsis.legacy.repository.annotations.JsonApiDelete;
import io.katharsis.legacy.repository.annotations.JsonApiFindAll;
import io.katharsis.legacy.repository.annotations.JsonApiFindAllWithIds;
import io.katharsis.legacy.repository.annotations.JsonApiFindOne;
import io.katharsis.legacy.repository.annotations.JsonApiResourceRepository;
import io.katharsis.legacy.repository.annotations.JsonApiSave;
import io.katharsis.queryspec.QuerySpec;

@JsonApiResourceRepository(TaskSubType.class)
public class TaskSubtypeRepository {

	private TaskRepository repo = new TaskRepository();

	private static final ConcurrentHashMap<Long, TaskSubType> map = new ConcurrentHashMap<>();

	public static void clear() {
		map.clear();
	}

	@JsonApiSave
	public <S extends TaskSubType> S save(S entity) {
		return repo.save(entity);
	}

	@JsonApiFindOne
	public TaskSubType findOne(Long aLong, QuerySpec querySpec) {
		return (TaskSubType) repo.findOne(aLong, querySpec);
	}

	@JsonApiFindAll
	public Iterable<TaskSubType> findAll(QuerySpec queryParams) {
		throw new UnsupportedOperationException();
	}

	@JsonApiFindAllWithIds
	public Iterable<TaskSubType> findAll(Iterable<Long> ids, QuerySpec queryParams) {
		throw new UnsupportedOperationException();
	}

	@JsonApiDelete
	public void delete(Long aLong) {
		throw new UnsupportedOperationException();
	}
}
