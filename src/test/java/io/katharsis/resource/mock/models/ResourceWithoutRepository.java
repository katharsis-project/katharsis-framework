package io.katharsis.resource.mock.models;

import io.katharsis.resource.annotations.JsonApiId;
import io.katharsis.resource.annotations.JsonApiResource;

@JsonApiResource(type = "resourceWithoutRepository")
public class ResourceWithoutRepository {

    @JsonApiId
    private String id;
}
