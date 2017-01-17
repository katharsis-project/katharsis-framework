package io.katharsis.resource.mock.models;

import java.util.List;

import io.katharsis.resource.annotations.JsonApiId;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.annotations.JsonApiToMany;
import io.katharsis.resource.annotations.JsonApiToOne;

@JsonApiResource(type = "lazy_tasks")
public class LazyTask {

    @JsonApiId
    private Long id;

    @JsonApiToMany
    private List<Project> projects;

    @JsonApiToOne
    private Project project;

    @JsonApiToOne(lazy = true)
    private Project lazyProject;

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

    public Project getLazyProject() {
        return lazyProject;
    }

    public void setLazyProject(Project lazyProject) {
        this.lazyProject = lazyProject;
    }
}
