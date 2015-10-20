package io.katharsis.resource.mock.models;

import io.katharsis.resource.annotations.*;

import java.util.List;

@JsonApiResource(type = "tasks")
public class Task {

    @JsonApiId
    private Long id;

    private String name;

    @JsonApiToOne
    @JsonApiIncludeByDefault
    private Project project;

    @JsonApiToMany(lazy = false)
    private List<Project> projects;

    public Long getId() {
        return id;
    }

    public Task setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(@SuppressWarnings("SameParameterValue") String name) {
        this.name = name;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }
}
