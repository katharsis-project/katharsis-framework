package io.katharsis.request.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.katharsis.jackson.deserializer.ResourceRelationshipsDeserializer;

public class DataBody {
    private String type;

    @JsonDeserialize(using = ResourceRelationshipsDeserializer.class)
    private ResourceLinks relationships;

    private Attributes attributes;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ResourceLinks getRelationships() {
        return relationships;
    }

    public void setRelationships(ResourceLinks relationships) {
        this.relationships = relationships;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }
}
