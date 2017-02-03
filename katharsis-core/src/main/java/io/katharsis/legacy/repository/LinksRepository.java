package io.katharsis.legacy.repository;

import io.katharsis.legacy.queryParams.QueryParams;
import io.katharsis.resource.links.LinksInformation;

/**
 * An optional interface that can be implemented along with
 * {@link ResourceRepository} or {@link RelationshipRepository} to get links
 * information about returned resource(s).
 * 
 * @deprecated Make use of LinksRepositoryV2 or ResourceList
 */
@Deprecated
public interface LinksRepository<T> {
	/**
	 * Return meta information about a resource. Can be called after find
	 * repository methods call
	 *
	 * <b>Consider the use of ResourceList instead.</b>
	 *
	 * @param resources
	 *            a list of found resource(s)
	 * @param queryParams
	 *            parameters sent along with the request
	 * @return meta information object
	 */
	LinksInformation getLinksInformation(Iterable<T> resources, QueryParams queryParams);
}
