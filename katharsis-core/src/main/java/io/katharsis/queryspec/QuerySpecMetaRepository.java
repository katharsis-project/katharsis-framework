package io.katharsis.queryspec;

import io.katharsis.repository.RelationshipRepository;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.response.MetaInformation;

/**
 * An optional interface that can be implemented along with {@link ResourceRepository} or {@link
 * RelationshipRepository} to get meta information about returned resource(s).
 */
public interface QuerySpecMetaRepository<T> {

	/**
	 * Return meta information about a resource. Can be called after find repository methods call
	 *
	 * @param resources a list of found resource(s)
	 * @param querySpec sent along with the request
	 * @return meta information object
	 */
	MetaInformation getMetaInformation(Iterable<T> resources, QuerySpec querySpec);
}
