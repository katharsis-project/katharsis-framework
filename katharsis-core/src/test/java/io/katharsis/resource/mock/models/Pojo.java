package io.katharsis.resource.mock.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.annotations.JsonApiToMany;
import io.katharsis.resource.annotations.JsonApiToOne;

@JsonApiResource(type = "pojo")
public class Pojo extends Thing {

    @JsonProperty("other-pojo")
    private OtherPojo otherPojo;

    @JsonProperty("some-project")
    @JsonApiToOne
    private Project project;

    @JsonProperty("some-projects")
    @JsonApiToMany
    private List<Project> projects;

    public OtherPojo getOtherPojo() {
        return otherPojo;
    }

    public void setOtherPojo(OtherPojo otherPojo) {
        this.otherPojo = otherPojo;
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
