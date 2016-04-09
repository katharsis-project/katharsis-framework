package io.katharsis.resource.mock.models;

import io.katharsis.resource.annotations.JsonApiId;
import io.katharsis.resource.annotations.JsonApiLinksInformation;
import io.katharsis.resource.annotations.JsonApiMetaInformation;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.response.LinksInformation;
import io.katharsis.response.MetaInformation;

@JsonApiResource(type = "projects")
public class Project {

    @JsonApiId
    private Long id;

    private String name;

    private String description;

    @JsonApiMetaInformation
    private MetaInformation metaInformation;

    @JsonApiLinksInformation
    private LinksInformation linksInformation;

    private ProjectData data;

    public Long getId() {
        return id;
    }

    public Project setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(@SuppressWarnings("SameParameterValue") String description) {
        this.description = description;
    }

    public MetaInformation getMetaInformation() {
        return metaInformation;
    }

    public Project setMetaInformation(MetaInformation metaInformation) {
        this.metaInformation = metaInformation;
        return this;
    }

    public LinksInformation getLinksInformation() {
        return linksInformation;
    }

    public Project setLinksInformation(LinksInformation linksInformation) {
        this.linksInformation = linksInformation;
        return this;
    }

    public ProjectData getData() {
        return data;
    }

    public void setData(ProjectData data) {
        this.data = data;
    }
}
