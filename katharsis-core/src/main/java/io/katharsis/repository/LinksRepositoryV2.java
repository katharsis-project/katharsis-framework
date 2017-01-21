package io.katharsis.repository;

import io.katharsis.legacy.repository.RelationshipRepository;
import io.katharsis.legacy.repository.ResourceRepository;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.resource.links.LinksInformation;

/**
 * An optional interface that can be implemented along with {@link ResourceRepository} or {@link
 * RelationshipRepository} to get links information about returned resource(s).
 * 
 * consisder the use ResourceList instead
 */
public interface LinksRepositoryV2<T> {

	/**
	 * Return meta information about a resource. Can be called after find repository methods call
	 *
	 * @param resources a list of found resource(s)
	 * @param querySpec sent along with the request
	 * @return meta information object
	 */
	LinksInformation getLinksInformation(Iterable<T> resources, QuerySpec querySpec);
}
