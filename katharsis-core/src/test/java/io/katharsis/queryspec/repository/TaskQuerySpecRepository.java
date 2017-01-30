package io.katharsis.queryspec.repository;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import io.katharsis.queryspec.QuerySpec;
import io.katharsis.repository.LinksRepositoryV2;
import io.katharsis.repository.MetaRepositoryV2;
import io.katharsis.repository.ResourceRepositoryBase;
import io.katharsis.resource.links.LinksInformation;
import io.katharsis.resource.list.ResourceList;
import io.katharsis.resource.meta.MetaInformation;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;

public class TaskQuerySpecRepository extends ResourceRepositoryBase<Task, Long>
		implements MetaRepositoryV2<Task>, LinksRepositoryV2<Task> {

	private static Set<Task> tasks = new HashSet<>();

	public TaskQuerySpecRepository() {
		super(Task.class);
	}

	@Override
	public ResourceList<Task> findAll(QuerySpec querySpec) {
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