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

    @JsonApiToOne
    @JsonApiLookupIncludeAutomatically
    private Project includedProject;

    @JsonApiToMany
    @JsonApiLookupIncludeAutomatically
    private List<Project> includedProjects;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Project getIncludedProject() {
        return includedProject;
    }

    public void setIncludedProject(Project includedProject) {
        this.includedProject = includedProject;
    }

    public List<Project> getIncludedProjects() {
        return includedProjects;
    }

    public void setIncludedProjects(List<Project> includedProjects) {
        this.includedProjects = includedProjects;
    }
}
