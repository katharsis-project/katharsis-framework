package io.katharsis.spring.domain.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.QuerySpecMetaRepository;
import io.katharsis.queryspec.QuerySpecResourceRepositoryBase;
import io.katharsis.response.MetaInformation;
import io.katharsis.spring.domain.model.Task;

@Component
public class TaskRepository extends QuerySpecResourceRepositoryBase<Task, String> implements QuerySpecMetaRepository<Task> {

	private Map<Long, Task> tasks = new HashMap<>();

	public TaskRepository() {
		super(Task.class);
		save(new Task(1L, "John"));
	}

	@Override
	public synchronized void delete(String id) {
		tasks.remove(id);
	}

	@Override
	public synchronized <S extends Task> S save(S task) {
		tasks.put(task.getId(), task);
		return task;
	}

	@Override
	public synchronized List<Task> findAll(QuerySpec querySpec) {
		return querySpec.apply(tasks.values());
	}

	@Override
	public MetaInformation getMetaInformation(Iterable<Task> resources, QuerySpec querySpec) {
		return new MetaInformation() {
			public String name = "meta information";
		};
	}
}
