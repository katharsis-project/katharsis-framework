package io.katharsis.itests.registry.fixtures.relationships;

import io.katharsis.resource.annotations.JsonApiId;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.annotations.JsonApiToOne;
import lombok.Data;

@Data
@JsonApiResource(type = "projects")
public class Project {
    @JsonApiId
    Integer id;

    @JsonApiToOne
    Task task;
}