package io.katharsis.jackson.mock.models;

import io.katharsis.resource.annotations.JsonApiId;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.annotations.JsonApiToMany;

import java.util.Collections;
import java.util.List;

@JsonApiResource(type = "classBs")
public class ClassB {

    @JsonApiId
    private Long id;

    @JsonApiToMany(lazy = false)
    private final List<ClassC> classCs;

    public ClassB(ClassC classCs) {
        this.classCs = Collections.singletonList(classCs);
    }

    public Long getId() {
        return id;
    }

    public ClassB setId(Long id) {
        this.id = id;
        return this;
    }

    public List<ClassC> getClassCs() {
        return classCs;
    }
}
