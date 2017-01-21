package io.katharsis.client.mock.repository;

import io.katharsis.client.mock.models.Schedule;
import io.katharsis.client.mock.models.Task;
import io.katharsis.repository.RelationshipRepositoryBase;

public class ScheduleToTaskRepository extends RelationshipRepositoryBase<Schedule, Long, Task, Long> {

	public ScheduleToTaskRepository() {
		super(Schedule.class, Task.class);
	}

}
