package io.katharsis.queryspec.repository;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.QuerySpecLinksRepository;
import io.katharsis.queryspec.QuerySpecMetaRepository;
import io.katharsis.queryspec.QuerySpecResourceRepositoryBase;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.response.LinksInformation;
import io.katharsis.response.MetaInformation;

public class TaskQuerySpecRepository extends QuerySpecResourceRepositoryBase<Task, Long>
		implements QuerySpecMetaRepository<Task>, QuerySpecLinksRepository<Task> {

	private static Set<Task> tasks = new HashSet<>();

	public TaskQuerySpecRepository() {
		super(Task.class);
	}

	@Override
	public Iterable<Task> findAll(QuerySpec querySpec) {
		return querySpec.apply(tasks);
	}

	@Override
	public <S extends Task> S save(S entity) {
		delete(entity.getId()); // replace current one
		
		// maintain bidirectional mapping, not perfect, should be done in the resources, but serves its purpose her.
		Project project = entity.getProject();
		if (project != null && !project.getTasks().contains(entity)) {
			project.getTasks().add(entity);
		}

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
	public LinksInformation getLinksInformation(Iterable<Task> resources, QuerySpec queryParams) {
		return new LinksInformation() {

			public String name = "value";
		};
	}

	@Override
	public MetaInformation getMetaInformation(Iterable<Task> resources, QuerySpec queryParams) {
		return new MetaInformation() {

			public String name = "value";
		};
	}

	public static void clear() {
		tasks.clear();
	}
}