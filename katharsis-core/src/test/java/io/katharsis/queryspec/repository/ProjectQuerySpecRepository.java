package io.katharsis.queryspec.repository;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import io.katharsis.queryspec.QuerySpec;
import io.katharsis.repository.ResourceRepositoryBase;
import io.katharsis.resource.list.ResourceList;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;

public class ProjectQuerySpecRepository extends ResourceRepositoryBase<Project, Long> {

	private static Set<Project> projects = new HashSet<>();

	public ProjectQuerySpecRepository() {
		super(Project.class);
	}

	@Override
	public ResourceList<Project> findAll(QuerySpec querySpec) {
		return querySpec.apply(projects);
	}

	@Override
	public <S extends Project> S save(S entity) {
		delete(entity.getId()); // replace current one

		// maintain bidirectional mapping, not perfect, should be done in the resources, but serves its purpose her.
		for (Task task : entity.getTasks()) {
			task.setProject(entity);
		}

		projects.add(entity);
		return entity;
	}

	@Override
	public void delete(Long id) {
		Iterator<Project> iterator = projects.iterator();
		while (iterator.hasNext()) {
			Project next = iterator.next();
			if (next.getId().equals(id)) {
				iterator.remove();
			}
		}
	}

	public static void clear() {
		projects.clear();
	}
}