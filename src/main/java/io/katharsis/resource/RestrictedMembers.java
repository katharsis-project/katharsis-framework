package io.katharsis.resource;

/**
 * A set of restricted members (fields) of a model
 *
 * See {@link <a href="http://jsonapi.org/format/#document-structure-resource-object-complex-attributes">Complex Attributes</a>}
 */
public enum RestrictedMembers {
    /**
     * Identifier of a resource
     */
    id,

    /**
     * Type of a resource
     */
    type,

    /**
     *  Information about a resource's relationships
     */
    links,

    /**
     *  Meta-information about a resource
     */
    meta,

    /**
     * Link to a resource
     * *
     */
    self
}
