package io.katharsis.resource.mock.models;

import java.util.List;

import io.katharsis.resource.annotations.JsonApiId;
import io.katharsis.resource.annotations.JsonApiLookupIncludeAutomatically;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.annotations.JsonApiToMany;

@JsonApiResource(type = "groups")
public class Group {

    @JsonApiId
    private Long id;

    @JsonApiToMany
    @JsonApiLookupIncludeAutomatically
    private List<Project>  projects;

    @JsonApiToMany
    @JsonApiLookupIncludeAutomatically
    private List<Memorandum> memoranda;

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

    public List<Memorandum> getMemoranda() {
        return memoranda;
    }

    public void setMemoranda(List<Memorandum> memos) {
        this.memoranda = memos;
    }
}
