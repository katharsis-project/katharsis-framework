package io.katharsis.jackson.mock.models;

import java.util.Collections;
import java.util.List;

import io.katharsis.resource.annotations.JsonApiId;
import io.katharsis.resource.annotations.JsonApiIncludeByDefault;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.annotations.JsonApiToMany;

@JsonApiResource(type = "classCsWithInclusion")
public class ClassCWithInclusion {
    @JsonApiId
    private Long id;

    @JsonApiToMany(lazy = false)
    @JsonApiIncludeByDefault
    private List<ClassCWithInclusion> classCsWithInclusion;

    public ClassCWithInclusion() {
    }

    public ClassCWithInclusion(ClassCWithInclusion classCsWithInclusion) {
        this.classCsWithInclusion = Collections.singletonList(classCsWithInclusion);
    }

    public Long getId() {
        return id;
    }

    public ClassCWithInclusion setId(Long id) {
        this.id = id;
        return this;
    }

    public List<ClassCWithInclusion> getClassCsWithInclusion() {
        return classCsWithInclusion;
    }

    public void setClassCsWithInclusion(List<ClassCWithInclusion> classCsWithInclusion) {
        this.classCsWithInclusion = classCsWithInclusion;
    }
}
