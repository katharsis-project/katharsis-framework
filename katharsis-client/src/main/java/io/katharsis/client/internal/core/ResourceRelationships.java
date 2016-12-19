package io.katharsis.client.internal.core;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * @see ResourceRelationshipsDeserializer
 */
public class ResourceRelationships {

    @JsonIgnore
    private final Map<String, Object> linkageList = new HashMap<>();
    
    @JsonIgnore
    private final Map<String, JsonNode> linksMap = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.linkageList;
    }
    
    @JsonIgnore
    public Map<String, JsonNode> getLinks() {
        return linksMap;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.linkageList.put(name, value);
    }
    
    @JsonIgnore
    public void setLinks(String name, JsonNode linksNode) {
        this.linksMap.put(name, linksNode);
    }
}
