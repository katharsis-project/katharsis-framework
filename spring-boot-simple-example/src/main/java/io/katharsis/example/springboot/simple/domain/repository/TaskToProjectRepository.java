/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.katharsis.example.springboot.simple.domain.repository;

import io.katharsis.example.springboot.simple.domain.model.Project;
import io.katharsis.example.springboot.simple.domain.model.Task;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.RelationshipRepository;
import io.katharsis.repository.annotations.*;
import io.katharsis.utils.PropertyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@JsonApiRelationshipRepository(source = Task.class, target = Project.class)
@Component
public class TaskToProjectRepository {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    @Autowired
    public TaskToProjectRepository(TaskRepository taskRepository, ProjectRepository projectRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
    }

    @JsonApiSetRelation
    public void setRelation(Task task, Long projectId, String fieldName) {
        Project project = projectRepository.findOne(projectId, null);
        try {
            PropertyUtils.setProperty(task, fieldName, project);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        taskRepository.save(task);
    }

    @JsonApiSetRelations
    public void setRelations(Task task, Iterable<Long> projectIds, String fieldName) {
        Iterable<Project> projects = projectRepository.findAll(projectIds, null);
        try {
            PropertyUtils.setProperty(task, fieldName, projects);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        taskRepository.save(task);
    }

    @JsonApiAddRelations
    public void addRelations(Task task, Iterable<Long> projectIds, String fieldName) {
        List<Project> newProjectList = new LinkedList<>();
        Iterable<Project> projectsToAdd = projectRepository.findAll(projectIds, null);
        projectsToAdd.forEach(newProjectList::add);
        try {
            if (PropertyUtils.getProperty(task, fieldName) != null) {
                Iterable<Project> projects = (Iterable<Project>) PropertyUtils.getProperty(task, fieldName);
                projects.forEach(newProjectList::add);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            PropertyUtils.setProperty(task, fieldName, newProjectList);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        taskRepository.save(task);

    }

    @JsonApiRemoveRelations
    public void removeRelations(Task task, Iterable<Long> projectIds, String fieldName) {
        try {
            if (PropertyUtils.getProperty(task, fieldName) != null) {
                Iterable<Project> projects = (Iterable<Project>) PropertyUtils.getProperty(task, fieldName);
                Iterator<Project> iterator = projects.iterator();
                while (iterator.hasNext()) {
                    for (Long projectIdToRemove : projectIds) {
                        if (iterator.next().getId().equals(projectIdToRemove)) {
                            iterator.remove();
                            break;
                        }
                    }
                }
                List<Project> newProjectList = new LinkedList<>();
                projects.forEach(newProjectList::add);

                PropertyUtils.setProperty(task, fieldName, newProjectList);
                taskRepository.save(task);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @JsonApiFindOneTarget
    public Project findOneTarget(Long taskId, String fieldName, QueryParams requestParams) {
        Task task = taskRepository.findOne(taskId, requestParams);
        try {
            return (Project) PropertyUtils.getProperty(task, fieldName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @JsonApiFindManyTargets
    public Iterable<Project> findManyTargets(Long taskId, String fieldName, QueryParams requestParams) {
        Task task = taskRepository.findOne(taskId, requestParams);
        try {
            return (Iterable<Project>) PropertyUtils.getProperty(task, fieldName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
