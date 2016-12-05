package io.katharsis.brave.mock.repository;

import io.katharsis.brave.mock.models.Project;
import io.katharsis.brave.mock.models.Task;
import io.katharsis.repository.RelationshipRepositoryBase;

public class ProjectToTaskRepository extends RelationshipRepositoryBase<Project, Long, Task, Long> {

	public ProjectToTaskRepository() {
		super(Project.class, Task.class);
	}
}
