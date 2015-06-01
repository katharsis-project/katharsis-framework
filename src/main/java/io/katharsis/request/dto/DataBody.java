package io.katharsis.request.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.katharsis.jackson.deserializer.ResourceRelationshipsDeserializer;

public class DataBody {
    private String type;

    @JsonDeserialize(using = ResourceRelationshipsDeserializer.class)
    private ResourceRelationships relationships;

    private Attributes attributes;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ResourceRelationships getRelationships() {
        return relationships;
    }

    public void setRelationships(ResourceRelationships relationships) {
        this.relationships = relationships;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }
}
