package io.katharsis.request.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.HashMap;
import java.util.Map;

public class Attributes {

    private final Map<String, Object> attributesMap = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Object> getAttributesMap() {
        return this.attributesMap;
    }

    @JsonAnySetter
    public void addAttribute(String name, Object value) {
        this.attributesMap.put(name, value);
    }
}
