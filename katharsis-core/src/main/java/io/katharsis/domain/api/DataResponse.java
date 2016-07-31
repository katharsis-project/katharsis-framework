package io.katharsis.domain.api;


import java.util.Collection;

/**
 * Primary data MUST be either:
 * - a single resource object, a single resource identifier object, or null, for requests that target single resources
 * - an array of resource objects, an array of resource identifier objects, or an empty array ([]), for requests that target resource collections
 * <p/>
 * http://jsonapi.org/format/#document-top-level
 */
public interface DataResponse extends TopLevel {

    <T> T getData();

    Collection<Resource> getIncluded();

}
