package io.katharsis.resource.mock.models;

import io.katharsis.resource.annotations.JsonApiResource;

@JsonApiResource(type = "specifications")
public class Specification extends Document {
    private String designOutlines;

    public String getDesignOutlines() {
        return designOutlines;
    }

    public void setDesignOutlines(String designOutlines) {
        this.designOutlines = designOutlines;
    }
}
