package io.katharsis.resource.field.attribute;

import io.katharsis.resource.field.ResourceField;

import java.util.Set;

/**
 * Provides a static set of attributes discovered at initialization time
 */
public class StaticResourceAttributesBridge implements ResourceAttributesBridge {

    private final Set<ResourceField> fields;
    public StaticResourceAttributesBridge(Set<ResourceField> fields) {
        this.fields = fields;
    }

    @Override
    public Set<ResourceField> getAttributes(Object resource) {
        return fields;
    }
}
