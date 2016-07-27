package io.katharsis.repository;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.response.MetaInformation;

/**
 * An optional interface that can be implemented along with {@link ResourceRepository} or {@link
 * RelationshipRepository} to get meta information about returned resource(s).
 */
public interface MetaRepository<T> {

    /**
     * Return meta information about a resource. Can be called after find repository methods call
     *
     * @param resources a list of found resource(s)
     * @param queryParams parameters sent along with the request
     * @return meta information object
     */
    MetaInformation getMetaInformation(Iterable<T> resources, QueryParams queryParams);
}
