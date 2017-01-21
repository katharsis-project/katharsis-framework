package io.katharsis.repository;

import java.io.Serializable;

import io.katharsis.core.internal.utils.MultivaluedMap;
import io.katharsis.queryspec.QuerySpec;

/**
 * {@code RelationshipRepositoryV2} implementation that provides additional support to bulk-request relations. 
 */
public interface BulkRelationshipRepositoryV2<T, I extends Serializable, D, J extends Serializable>
		extends RelationshipRepositoryV2<T, I, D, J> {

	/**
	 * Bulk request multiple targets at once. 
	 */
	MultivaluedMap<I, D> findTargets(Iterable<I> sourceIds, String fieldName, QuerySpec querySpec);

}
