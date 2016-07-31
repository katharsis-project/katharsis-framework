package io.katharsis.dispatcher.registry.api;

import io.katharsis.domain.api.LinksInformation;
import io.katharsis.query.QueryParams;
import io.katharsis.repository.RelationshipRepository;
import io.katharsis.repository.RepositoryParameterProvider;
import io.katharsis.repository.ResourceRepository;

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
    LinksInformation getLinksInformation(RepositoryParameterProvider parameterProvider, Iterable<T> resources, QueryParams queryParams);
}
