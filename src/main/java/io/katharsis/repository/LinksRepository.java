package io.katharsis.repository;

import io.katharsis.queryParams.RequestParams;
import io.katharsis.response.LinksInformation;

/**
 * An optional interface that can be implemented along with {@link ResourceRepository} or {@link
 * RelationshipRepository} to get links information about returned resource(s).
 */
public interface LinksRepository<T> {
    /**
     * Return meta information about a resource. Can be called after find repository methods call
     *
     * @param resources a list of found resource(s)
     * @param requestParams parameters sent along with the request
     * @return meta information object
     */
    LinksInformation getLinksInformation(Iterable<T> resources, RequestParams requestParams);
}
