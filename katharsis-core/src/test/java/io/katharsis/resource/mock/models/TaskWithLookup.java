package io.katharsis.resource.mock.models;

import io.katharsis.resource.annotations.JsonApiId;
import io.katharsis.resource.annotations.JsonApiLookupIncludeAutomatically;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.annotations.JsonApiToOne;

@JsonApiResource(type = "task-with-lookup")
public class TaskWithLookup {

    @JsonApiId
    private String id;

    @JsonApiToOne
    @JsonApiLookupIncludeAutomatically
    private Project project;

    @JsonApiToOne
    @JsonApiLookupIncludeAutomatically
    private Project projectNull;

    @JsonApiToOne
    @JsonApiLookupIncludeAutomatically(overwrite = true)
    private Project projectOverridden;

    @JsonApiToOne
    @JsonApiLookupIncludeAutomatically(overwrite = true)
    private Project projectOverriddenNull;

    public String getId() {
        return id;
    }

    public TaskWithLookup setId(String id) {
        this.id = id;
        return this;
    }

    public Project getProject() {
        return project;
    }

    public TaskWithLookup setProject(Project project) {
        this.project = project;
        return this;
    }

    public Project getProjectNull() {
        return projectNull;
    }

    public TaskWithLookup setProjectNull(Project projectNull) {
        this.projectNull = projectNull;
        return this;
    }

    public Project getProjectOverridden() {
        return projectOverridden;
    }

    public TaskWithLookup setProjectOverridden(Project projectOverridden) {
        this.projectOverridden = projectOverridden;
        return this;
    }

    public Project getProjectOverriddenNull() {
        return projectOverriddenNull;
    }

    public TaskWithLookup setProjectOverriddenNull(Project projectOverriddenNull) {
        this.projectOverriddenNull = projectOverriddenNull;
        return this;
    }
}
