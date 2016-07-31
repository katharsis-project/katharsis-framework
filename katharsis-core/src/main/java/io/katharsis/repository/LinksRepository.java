package io.katharsis.repository;

import io.katharsis.domain.api.LinksInformation;
import io.katharsis.queryParams.QueryParams;

/**
 * An optional interface that can be implemented along with {@link ResourceRepository} or {@link
 * RelationshipRepository} to get links information about returned resource(s).
 */
public interface LinksRepository<T> {
    /**
     * Return meta information about a resource. Can be called after find repository methods call
     *
     * @param resources a list of found resource(s)
     * @param queryParams parameters sent along with the request
     * @return meta information object
     */
    LinksInformation getLinksInformation(Iterable<T> resources, QueryParams queryParams);
}
