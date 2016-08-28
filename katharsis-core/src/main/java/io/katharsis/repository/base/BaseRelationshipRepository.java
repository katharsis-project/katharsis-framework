package io.katharsis.repository.base;

import java.io.Serializable;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.RelationshipRepository;
import io.katharsis.repository.base.query.DefaultQuerySpecParser;
import io.katharsis.repository.base.query.QuerySpec;
import io.katharsis.repository.base.query.QuerySpecParser;
import io.katharsis.resource.registry.ResourceRegistry;

/**
 * RelationshipRepository base implementation preprocessing incoming QueryParams
 * to QuerySpec.
 */
public abstract class BaseRelationshipRepository<T, T_ID extends Serializable, D, D_ID extends Serializable>
		implements RelationshipRepository<T, T_ID, D, D_ID> {

	private QuerySpecParser specParser;

	public BaseRelationshipRepository(ResourceRegistry resourceRegistry) {
		this.specParser = new DefaultQuerySpecParser(resourceRegistry);
	}

	@Override
	public final D findOneTarget(T_ID sourceId, String fieldName, QueryParams queryParams) {
		return findOneTarget(sourceId, fieldName, specParser.fromParams(queryParams));
	}

	protected abstract D findOneTarget(T_ID sourceId, String fieldName, QuerySpec fromParams);

	@Override
	public final Iterable<D> findManyTargets(T_ID sourceId, String fieldName, QueryParams queryParams) {
		return findManyTargets(sourceId, fieldName, specParser.fromParams(queryParams));
	}

	protected abstract Iterable<D> findManyTargets(T_ID sourceId, String fieldName, QuerySpec fromParams);

}
