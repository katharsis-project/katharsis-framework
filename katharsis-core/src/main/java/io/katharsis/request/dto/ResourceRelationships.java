package io.katharsis.request.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.katharsis.jackson.deserializer.ResourceRelationshipsDeserializer;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @see ResourceRelationshipsDeserializer
 */
@Data
public class ResourceRelationships {

    @JsonIgnore
    private final Map<String, Object> linkageList = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.linkageList;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.linkageList.put(name, value);
    }
}
