package io.katharsis.jackson.mock.models;

import io.katharsis.resource.annotations.*;

import java.util.Collections;
import java.util.List;

@JsonApiResource(type = "classBs")
public class ClassB {

    @JsonApiId
    private Long id;

    @JsonApiToMany(lazy = false)
    private final List<ClassC> classCs;

    @JsonApiToOne
    private final ClassC classC;

    @JsonApiToOne
    @JsonApiIncludeByDefault
    private final ClassA classA;

    public ClassB() {
        this.classCs = null;
        this.classC = null;
        this.classA = null;
    }

    public ClassB(ClassC classCs, ClassC classC) {
        this.classCs = Collections.singletonList(classCs);
        this.classC = classC;
        this.classA = null;
    }

    public ClassB(ClassA classA) {
        this.classA = classA;
        this.classC = null;
        this.classCs = null;
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

    public ClassC getClassC() {
        return classC;
    }

    public ClassA getClassA() {
        return classA;
    }
}
