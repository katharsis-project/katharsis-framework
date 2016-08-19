package io.katharsis.client;

import java.io.Serializable;
import java.util.List;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.RelationshipRepository;

/**
 * Implemented by every {@link RelationshipRepository} stub.
 */
public interface RelationshipRepositoryStub<T, T_ID extends Serializable, D, D_ID extends Serializable>
		extends RelationshipRepository<T, T_ID, D, D_ID> {

	@Override
	public List<D> findManyTargets(T_ID sourceId, String fieldName, QueryParams queryParams);


}
