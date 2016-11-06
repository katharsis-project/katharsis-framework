package io.katharsis.queryspec;

import java.io.Serializable;
import java.util.Map;

import io.katharsis.utils.MultivaluedMap;

/**
 * {@code QuerySpecRelationshipRepository} implementation that provides additional support to bulk-request relations. 
 */
public interface QuerySpecBulkRelationshipRepository<T, I extends Serializable, D, J extends Serializable>
		extends QuerySpecRelationshipRepository<T, I, D, J> {

	/**
	 * Bulk request multiple targets at once. 
	 */
	MultivaluedMap<I, D> findTargets(Iterable<I> sourceIds, String fieldName, QuerySpec querySpec);

}
