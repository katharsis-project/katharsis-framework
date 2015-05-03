package io.katharsis.request.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.Map;

public class ResourceLinks {

    @JsonIgnore
    private Map<String, Linkage> linkageList = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Linkage> getAdditionalProperties() {
        return this.linkageList;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Linkage value) {
        this.linkageList.put(name, value);
    }
}
