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

    @JsonApiToMany
    private final List<ClassC> classCs;

    public ClassB(ClassC classCs) {
        this.classCs = Collections.singletonList(classCs);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<ClassC> getClassCs() {
        return classCs;
    }
}
