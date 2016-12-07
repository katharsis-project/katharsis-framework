package io.katharsis.spring.domain.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import io.katharsis.queryspec.QuerySpec;
import io.katharsis.repository.ResourceRepositoryBase;
import io.katharsis.resource.list.ResourceList;
import io.katharsis.spring.domain.model.Project;

@Component
public class ProjectRepository extends ResourceRepositoryBase<Project, String> {

	private Map<Long, Project> projects = new HashMap<>();

	public ProjectRepository() {
		super(Project.class);
		List<String> interests = new ArrayList<>();
		interests.add("coding");
		interests.add("art");
		save(new Project(1L, "Project A"));
		save(new Project(2L, "Project B"));
		save(new Project(3L, "Project C"));
	}

	@Override
	public synchronized void delete(String id) {
		projects.remove(id);
	}

	@Override
	public synchronized <S extends Project> S save(S project) {
		projects.put(project.getId(), project);
		return project;
	}

	@Override
	public synchronized ResourceList<Project> findAll(QuerySpec querySpec) {
		return querySpec.apply(projects.values());
	}
}
