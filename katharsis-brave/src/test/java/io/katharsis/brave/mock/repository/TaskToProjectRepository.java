package io.katharsis.brave.mock.repository;

import io.katharsis.brave.mock.models.Project;
import io.katharsis.brave.mock.models.Task;
import io.katharsis.repository.RelationshipRepositoryBase;

public class TaskToProjectRepository extends RelationshipRepositoryBase<Task, Long, Project, Long> {

	public TaskToProjectRepository() {
		super(Task.class, Project.class);
	}
}
