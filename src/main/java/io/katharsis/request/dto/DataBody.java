package io.katharsis.request.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.Map;

public class DataBody {
    private String type;
    private ResourceLinks links;

    @JsonIgnore
    private Map<String, Object> basicFields = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.basicFields;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.basicFields.put(name, value);
    }

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
}
