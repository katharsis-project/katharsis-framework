package io.katharsis.rs.resource.repository;

import io.katharsis.repository.RelationshipRepository;
import io.katharsis.rs.resource.model.Project;
import io.katharsis.rs.resource.model.Task;
import org.jvnet.hk2.annotations.Service;

@Service
public class TaskToProjectRepository implements RelationshipRepository<Task, Long, Project, Long> {

    @Override
    public void addRelation(Task task, Project project) {

    }

    @Override
    public void removeRelation(Task task, Project project) {

    }

    @Override
    public Project findOneTarget(Long aLong) {
        return null;
    }

    @Override
    public Iterable<Project> findTarget(Long aLong) {
        return null;
    }

    @Override
    public Long findOneTargetId(Long aLong) {
        return null;
    }

    @Override
    public Iterable<Long> findTargetIds(Long aLong) {
        return null;
    }
}
