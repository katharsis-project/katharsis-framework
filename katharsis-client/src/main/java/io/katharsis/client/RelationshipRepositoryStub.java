package io.katharsis.client;

import java.io.Serializable;
import java.util.List;

import io.katharsis.legacy.queryParams.QueryParams;
import io.katharsis.legacy.repository.RelationshipRepository;

/**
 * Implemented by every {@link RelationshipRepository} stub.
 * 
 * @deprecated make use of QuerySpec
 */
@Deprecated
public interface RelationshipRepositoryStub<T, TID extends Serializable, D, DID extends Serializable>
		extends RelationshipRepository<T, TID, D, DID> {

	@Override
	public List<D> findManyTargets(TID sourceId, String fieldName, QueryParams queryParams);


}
