package io.katharsis.security.model;

import java.util.HashMap;
import java.util.Map;

import io.katharsis.queryspec.QuerySpec;
import io.katharsis.repository.ResourceRepositoryBase;
import io.katharsis.resource.list.DefaultResourceList;
import io.katharsis.resource.list.ResourceList;
import io.katharsis.security.ResourcePermissionInformationImpl;


public class ProjectRepository extends ResourceRepositoryBase<Project, Long> {

	private static final Map<Long, Project> PROJECTS = new HashMap<>();

	public ProjectRepository() {
		super(Project.class);
	}

	@Override
	public <S extends Project> S save(S entity) {
		PROJECTS.put(entity.getId(), entity);
		return entity;
	}

	@Override
	public ResourceList<Project> findAll(QuerySpec querySpec) {
		DefaultResourceList<Project> list = querySpec.apply(PROJECTS.values());
		list.setMeta(new ResourcePermissionInformationImpl());
		return list;
	}

	@Override
	public void delete(Long id) {
		PROJECTS.remove(id);
	}

	public static void clear() {
		PROJECTS.clear();
	}
}
