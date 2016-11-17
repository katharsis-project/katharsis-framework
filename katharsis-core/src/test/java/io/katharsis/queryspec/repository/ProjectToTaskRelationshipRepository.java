package io.katharsis.queryspec.repository;

import io.katharsis.repository.RelationshipRepositoryBase;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;

public class ProjectToTaskRelationshipRepository extends RelationshipRepositoryBase<Project, Long, Task, Long> {

	public ProjectToTaskRelationshipRepository() {
		super(Project.class, Task.class);
	}

}