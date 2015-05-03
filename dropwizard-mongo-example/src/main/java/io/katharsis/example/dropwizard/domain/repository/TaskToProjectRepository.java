package io.katharsis.example.dropwizard.domain.repository;

import io.katharsis.example.dropwizard.domain.model.Project;
import io.katharsis.example.dropwizard.domain.model.Task;
import io.katharsis.repository.RelationshipRepository;
import org.apache.commons.beanutils.PropertyUtils;
import org.bson.types.ObjectId;

import javax.inject.Inject;

public class TaskToProjectRepository implements RelationshipRepository<Task, ObjectId, Project, ObjectId> {

    private TaskRepository taskRepository;

    @Inject
    public TaskToProjectRepository(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public void addRelation(Task task, ObjectId objectId, String s) {

    }

    @Override
    public void removeRelation(Task task, ObjectId objectId, String s) {

    }

    @Override
    public Project findOneTarget(ObjectId objectId, String s) {
        Task task = taskRepository.findOne(objectId);
        try {
            return (Project) PropertyUtils.getProperty(task, s);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Iterable<Project> findTargets(ObjectId objectId, String s) {
        Task task = taskRepository.findOne(objectId);
        try {
            return (Iterable<Project>) PropertyUtils.getProperty(task, s);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
