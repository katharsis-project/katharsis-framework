package ch.adnovum.jcan.katharsis.mock.repository;

import javax.enterprise.context.ApplicationScoped;

import ch.adnovum.jcan.katharsis.mock.models.Project;
import ch.adnovum.jcan.katharsis.mock.models.Task;
import io.katharsis.queryspec.QuerySpecRelationshipRepositoryBase;

@ApplicationScoped
public class TaskToProjectRepository extends QuerySpecRelationshipRepositoryBase<Task, Long, Project, Long> {

	public TaskToProjectRepository() {
		super(Task.class, Project.class);
	}

}
