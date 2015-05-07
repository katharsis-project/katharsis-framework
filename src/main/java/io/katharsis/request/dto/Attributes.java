package io.katharsis.request.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.HashMap;
import java.util.Map;

public class Attributes {
    private Map<String, Object> attributes = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    @JsonAnySetter
    public void addAttribute(String name, Object value) {
        this.attributes.put(name, value);
    }
}
