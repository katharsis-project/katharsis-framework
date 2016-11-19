package io.katharsis.client.mock.repository;

import io.katharsis.client.mock.models.Schedule;
import io.katharsis.client.mock.models.Task;
import io.katharsis.repository.RelationshipRepositoryBase;

public class TaskToScheduleRepo extends RelationshipRepositoryBase<Task, Long, Schedule, Long> {

	public TaskToScheduleRepo() {
		super(Task.class, Schedule.class);
	}

}
