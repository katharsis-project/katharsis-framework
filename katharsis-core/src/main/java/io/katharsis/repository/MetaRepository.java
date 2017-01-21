package io.katharsis.repository;

import io.katharsis.legacy.queryParams.QueryParams;
import io.katharsis.resource.meta.MetaInformation;

/**
 * An optional interface that can be implemented along with {@link ResourceRepository} or {@link
 * RelationshipRepository} to get meta information about returned resource(s).
 * 
 * <b>Consider the use of ResourceList instead.</b>
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
