package io.katharsis.client.internal.core;

/**
 * Defines a container type that is being passed between the serializers.
 * Helps with processing additional relationship data that might need to be present.
 */
public enum ContainerType {
    TOP,
    INCLUDED,
    INCLUDED_NESTED
}
