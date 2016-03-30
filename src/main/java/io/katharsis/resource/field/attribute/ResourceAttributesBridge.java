package io.katharsis.resource.field.attribute;

import io.katharsis.resource.field.ResourceField;

import java.util.Set;

/**
 * Classes which implement those interface are able to provide a set of resource attributes
 */
public interface ResourceAttributesBridge {

    /**
     * Return a list of resource attributes
     * @param resource a resource to be analyzed
     * @return a set of attributes
     */
    Set<ResourceField> getAttributes(Object resource);

    void setProperty(Object instance, String propertyName, Object property);
}
