package io.katharsis.queryspec.repository;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.QuerySpecResourceRepository;
import io.katharsis.repository.LinksRepository;
import io.katharsis.repository.MetaRepository;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.response.LinksInformation;
import io.katharsis.response.MetaInformation;

public class TestQuerySpecResourceRepository implements QuerySpecResourceRepository<Task, Long>, MetaRepository<Task>, LinksRepository<Task> {

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
	public Iterable<Task> findAll(QuerySpec querySpec) {
		if (querySpec == null) {
			return tasks;
		}
		return querySpec.apply(tasks);
	}

	@Override
	public Iterable<Task> findAll(Iterable<Long> ids, QuerySpec querySpec) {
		if (querySpec == null) {
			return tasks;
		}
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

	@Override
	public LinksInformation getLinksInformation(Iterable<Task> resources, QueryParams queryParams) {
		return new LinksInformation() {

			public String name = "value";
		};
	}

	@Override
	public MetaInformation getMetaInformation(Iterable<Task> resources, QueryParams queryParams) {
		return new MetaInformation() {

			public String name = "value";
		};
	}

	public static void clear() {
		tasks.clear();
	}
}