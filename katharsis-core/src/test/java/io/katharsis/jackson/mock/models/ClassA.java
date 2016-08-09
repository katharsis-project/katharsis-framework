package io.katharsis.jackson.mock.models;

import io.katharsis.resource.annotations.JsonApiId;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.annotations.JsonApiToMany;

import java.util.Collections;
import java.util.List;

@JsonApiResource(type = "classAs")
public class ClassA {

    @JsonApiId
    private Long id;

    @JsonApiToMany(lazy = false)
    private final List<ClassB> classBs;

    public ClassA(ClassB classBs) {
    	if(classBs != null)
    		this.classBs = Collections.singletonList(classBs);
    	else
    		this.classBs = Collections.emptyList();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<ClassB> getClassBs() {
        return classBs;
    }
}
