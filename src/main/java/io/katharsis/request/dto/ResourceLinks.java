package io.katharsis.request.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.Map;

/**
 * @see io.katharsis.jackson.deserializer.ResourceLinksDeserializer
 */
public class ResourceLinks {

    @JsonIgnore
    private Map<String, Object> linkageList = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.linkageList;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.linkageList.put(name, value);
    }
}
