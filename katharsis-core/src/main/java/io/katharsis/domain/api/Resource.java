package io.katharsis.domain.api;

import java.util.Map;

/**
 * “Resource objects” appear in a JSON API document to represent resources.
 * <p/>
 * http://jsonapi.org/format/#document-resource-objects
 */
public interface Resource extends ResourceId{

    Map<String, Object> getAttributes();

    Map<String, Relationship> getRelationships();

}
