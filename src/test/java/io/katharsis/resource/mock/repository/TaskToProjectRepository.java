package io.katharsis.resource.mock.repository;

import io.katharsis.repository.RelationshipRepository;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;

public class TaskToProjectRepository implements RelationshipRepository<Task, Project> {
    @Override
    public void addRelation(Task source, Project destination) {

    }

    @Override
    public void removeRelation(Task source, Project destination) {

    }
}
