package io.katharsis.domain.api;

/**
 * A “resource identifier object” is an object that identifies an individual resource.
 * A “resource identifier object” MUST contain type and id members.
 * A “resource identifier object” MAY also include a meta member, whose value is a meta object
 * that contains non-standard meta-information.
 * <p/>
 * http://jsonapi.org/format/#document-resource-identifier-objects
 */
public interface ResourceId {

    <T> T getId();

    String getType();

}
