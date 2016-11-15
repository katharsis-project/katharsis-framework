package io.katharsis.jackson.mock.models;

import io.katharsis.resource.annotations.JsonApiId;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.annotations.JsonApiToMany;

import java.util.List;

@JsonApiResource(type = "classCs")
public class ClassC {

    @JsonApiId
    private Long id;

    @JsonApiToMany
    private List<ClassA> classAs;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<ClassA> getClassAs() {
        return classAs;
    }

    public void setClassAs(List<ClassA> classAs) {
        this.classAs = classAs;
    }
}
