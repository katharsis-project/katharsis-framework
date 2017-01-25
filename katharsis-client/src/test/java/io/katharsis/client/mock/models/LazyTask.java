package io.katharsis.client.mock.models;

import io.katharsis.resource.annotations.JsonApiId;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.annotations.JsonApiToMany;
import io.katharsis.resource.annotations.JsonApiToOne;

import java.util.List;

@JsonApiResource(type = "lazy_tasks")
public class LazyTask {

    @JsonApiId
    private Long id;

    @JsonApiToOne(lazy = true)
    private Project lazyProject;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Project getLazyProject() {
        return lazyProject;
    }

    public void setLazyProject(Project lazyProject) {
        this.lazyProject = lazyProject;
    }
}
