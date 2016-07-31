package io.katharsis.resource.mock.models;

import io.katharsis.domain.api.LinksInformation;
import io.katharsis.domain.api.MetaInformation;
import io.katharsis.resource.annotations.JsonApiId;
import io.katharsis.resource.annotations.JsonApiIncludeByDefault;
import io.katharsis.resource.annotations.JsonApiLinksInformation;
import io.katharsis.resource.annotations.JsonApiMetaInformation;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.annotations.JsonApiToMany;

import java.util.List;

@JsonApiResource(type = "users")
public class User {

    @JsonApiId
    private Long id;

    private String name;

    @JsonApiToMany(lazy = false)
    @JsonApiIncludeByDefault
    private List<Project> assignedProjects;

    @JsonApiMetaInformation
    private MetaInformation metaInformation;

    @JsonApiLinksInformation
    private LinksInformation linksInformation;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Project> getAssignedProjects() {
        return assignedProjects;
    }

    public void setAssignedProjects(List<Project> assignedProjects) {
        this.assignedProjects = assignedProjects;
    }

    public MetaInformation getMetaInformation() {
        return metaInformation;
    }

    public void setMetaInformation(MetaInformation metaInformation) {
        this.metaInformation = metaInformation;
    }

    public LinksInformation getLinksInformation() {
        return linksInformation;
    }

    public void setLinksInformation(LinksInformation linksInformation) {
        this.linksInformation = linksInformation;
    }
}
