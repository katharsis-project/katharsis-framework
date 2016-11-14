package io.katharsis.spring.domain.repository;

import org.springframework.stereotype.Component;

import io.katharsis.queryspec.QuerySpecRelationshipRepositoryBase;
import io.katharsis.spring.domain.model.Project;
import io.katharsis.spring.domain.model.Task;

@Component
public class TaskToProjectRepository extends QuerySpecRelationshipRepositoryBase<Task, Long, Project, Long> {

	protected TaskToProjectRepository() {
		super(Task.class, Project.class);
	}

}
