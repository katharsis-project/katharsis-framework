package io.katharsis.queryspec.repository;

import io.katharsis.queryspec.QuerySpecRelationshipRepositoryBase;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;

public class ProjectToTaskRelationshipRepository extends QuerySpecRelationshipRepositoryBase<Project, Long, Task, Long> {

	public ProjectToTaskRelationshipRepository() {
		super(Project.class, Task.class);
	}

}