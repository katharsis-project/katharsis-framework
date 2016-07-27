package io.katharsis.resource.mock.models;

import io.katharsis.resource.annotations.*;

import java.util.List;

@JsonApiResource(type = "lazy_tasks")
public class LazyTask {

    @JsonApiId
    private Long id;

    @JsonApiToMany
    private List<Project> projects;

    @JsonApiToOne
    private Project project;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
