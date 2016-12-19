package io.katharsis.client.internal.core;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class Attributes {

    @Getter(onMethod = @__(@JsonAnyGetter))
    private final Map<String, Object> attributesMap = new HashMap<>();

    @JsonAnySetter
    public void addAttribute(String name, Object value) {
        this.attributesMap.put(name, value);
    }
}
