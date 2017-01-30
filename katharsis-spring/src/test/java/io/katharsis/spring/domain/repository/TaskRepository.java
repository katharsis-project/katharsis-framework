package io.katharsis.spring.domain.repository;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import io.katharsis.queryspec.QuerySpec;
import io.katharsis.repository.ResourceRepositoryBase;
import io.katharsis.resource.list.DefaultResourceList;
import io.katharsis.resource.list.ResourceList;
import io.katharsis.resource.meta.MetaInformation;
import io.katharsis.spring.domain.model.Task;

@Component
public class TaskRepository extends ResourceRepositoryBase<Task, String> {

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
	public synchronized ResourceList<Task> findAll(QuerySpec querySpec) {
		DefaultResourceList<Task> list = querySpec.apply(tasks.values());
		list.setMeta(new MetaInformation() {

			public String name = "meta information";
		});
		return list;
	}
}
