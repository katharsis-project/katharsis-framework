package io.katharsis.request.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.katharsis.jackson.deserializer.ResourceLinksDeserializer;

public class DataBody {
    private String type;

    @JsonDeserialize(using = ResourceLinksDeserializer.class)
    private ResourceLinks links;

    private Attributes attributes;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ResourceLinks getLinks() {
        return links;
    }

    public void setLinks(ResourceLinks links) {
        this.links = links;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }
}
