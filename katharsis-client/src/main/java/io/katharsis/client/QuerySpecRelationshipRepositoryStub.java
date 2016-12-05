package io.katharsis.client;

import java.io.Serializable;

import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.QuerySpecRelationshipRepository;
import io.katharsis.repository.RelationshipRepository;
import io.katharsis.resource.list.DefaultResourceList;

/**
 * Implemented by every {@link RelationshipRepository} stub.
 */
public interface QuerySpecRelationshipRepositoryStub<T, TID extends Serializable, D, DID extends Serializable>
		extends QuerySpecRelationshipRepository<T, TID, D, DID> {

	@Override
	public DefaultResourceList<D> findManyTargets(TID sourceId, String fieldName, QuerySpec querySpec);

}
