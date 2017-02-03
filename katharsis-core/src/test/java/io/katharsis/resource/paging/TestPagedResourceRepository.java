package io.katharsis.resource.paging;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.katharsis.queryspec.QuerySpec;
import io.katharsis.repository.ResourceRepositoryV2;
import io.katharsis.resource.list.ResourceList;
import io.katharsis.resource.mock.models.Task;

public class TestPagedResourceRepository implements ResourceRepositoryV2<Task, Long> {

	private static List<Task> tasks = new ArrayList<Task>();

	@Override
	public Class<Task> getResourceClass() {
		return Task.class;
	}

	@Override
	public Task findOne(Long id, QuerySpec querySpec) {
		for (Task task : tasks) {
			if (task.getId().equals(id)) {
				return task;
			}
		}
		return null;
	}

	@Override
	public ResourceList<Task> findAll(QuerySpec querySpec) {
		return querySpec.apply(tasks);
	}

	@Override
	public ResourceList<Task> findAll(Iterable<Long> ids, QuerySpec querySpec) {
		return querySpec.apply(tasks);
	}

	@Override
	public <S extends Task> S save(S entity) {
		tasks.add(entity);
		return null;
	}

	@Override
	public void delete(Long id) {
		Iterator<Task> iterator = tasks.iterator();
		while (iterator.hasNext()) {
			Task next = iterator.next();
			if (next.getId().equals(id)) {
				iterator.remove();
			}
		}
	}

	public static void clear() {
		tasks.clear();
	}

	@Override
	public <S extends Task> S create(S entity) {
		return save(entity);
	}
}