package io.katharsis.jackson.mock.models;

import io.katharsis.resource.annotations.JsonApiId;
import io.katharsis.resource.annotations.JsonApiIncludeByDefault;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.annotations.JsonApiToMany;

import java.util.Collections;
import java.util.List;

@JsonApiResource(type = "classBsWithInclusion")
public class ClassBWithInclusion {
    @JsonApiId
    private Long id;

    @JsonApiToMany
    @JsonApiIncludeByDefault
    private List<ClassCWithInclusion> classCsWithInclusion;

    public ClassBWithInclusion() {
    }

    public ClassBWithInclusion(ClassCWithInclusion classCsWithInclusion) {
        this.classCsWithInclusion = Collections.singletonList(classCsWithInclusion);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<ClassCWithInclusion> getClassCsWithInclusion() {
        return classCsWithInclusion;
    }

    public void setClassCsWithInclusion(List<ClassCWithInclusion> classCsWithInclusion) {
        this.classCsWithInclusion = classCsWithInclusion;
    }
}
