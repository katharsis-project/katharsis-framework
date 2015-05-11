package io.katharsis.example.dropwizard.domain.repository;

import io.katharsis.example.dropwizard.domain.model.Project;
import io.katharsis.example.dropwizard.domain.model.Task;
import io.katharsis.repository.RelationshipRepository;
import org.apache.commons.beanutils.PropertyUtils;
import org.bson.types.ObjectId;

import javax.inject.Inject;

public class TaskToProjectRepository implements RelationshipRepository<Task, ObjectId, Project, ObjectId> {

    private TaskRepository taskRepository;
    private ProjectRepository projectRepository;

    @Inject
    public TaskToProjectRepository(TaskRepository taskRepository, ProjectRepository projectRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
    }

    @Override
    public void setRelation(Task task, ObjectId projectId, String fieldName) {
        Project project = projectRepository.findOne(projectId);
        try {
            PropertyUtils.setProperty(task, fieldName, project);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        taskRepository.save(task);
    }

    @Override
    public void setRelations(Task task, Iterable<ObjectId> projectIds, String fieldName) {
        Iterable<Project> projects = projectRepository.findAll(projectIds, null);
        try {
            PropertyUtils.setProperty(task, fieldName, projects);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        taskRepository.save(task);
    }

    @Override
    public Project findOneTarget(ObjectId objectId, String fieldName) {
        Task task = taskRepository.findOne(objectId);
        try {
            return (Project) PropertyUtils.getProperty(task, fieldName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Iterable<Project> findTargets(ObjectId objectId, String fieldName) {
        Task task = taskRepository.findOne(objectId);
        try {
            return (Iterable<Project>) PropertyUtils.getProperty(task, fieldName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
