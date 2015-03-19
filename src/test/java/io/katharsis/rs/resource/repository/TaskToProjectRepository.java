package io.katharsis.rs.resource.repository;

import io.katharsis.repository.RelationshipRepository;
import io.katharsis.rs.resource.model.Project;
import io.katharsis.rs.resource.model.Task;
import org.jvnet.hk2.annotations.Service;

@Service
public class TaskToProjectRepository implements RelationshipRepository<Task, Project> {
    @Override
    public void addRelation(Task source, Project destination) {

    }

    @Override
    public void removeRelation(Task source, Project destination) {

    }
}
