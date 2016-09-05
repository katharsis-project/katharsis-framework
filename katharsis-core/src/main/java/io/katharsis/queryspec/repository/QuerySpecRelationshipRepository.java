package io.katharsis.queryspec.repository;

import java.io.Serializable;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryspec.DefaultQuerySpecConverter;
import io.katharsis.queryspec.FilterOperatorRegistry;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.QuerySpecConverter;
import io.katharsis.repository.RelationshipRepository;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ResourceRegistryAware;

/**
 * QuerySpec-based RelationshipRepository that translates incoming
 * QueryParameters to QuerySpec for ease of use.
 */
public abstract class QuerySpecRelationshipRepository<T, T_ID extends Serializable, D, D_ID extends Serializable>
		implements ResourceRegistryAware, RelationshipRepository<T, T_ID, D, D_ID> {

	private QuerySpecConverter specParser;

	protected abstract Class<D> getResourceClass();

	@Override
	public void setResourceRegistry(ResourceRegistry resourceRegistry) {
		FilterOperatorRegistry filterOperators = new FilterOperatorRegistry();
		setupFilterOperators(filterOperators);
		this.specParser = new DefaultQuerySpecConverter(resourceRegistry, filterOperators);
	}

	/**
	 * Use this method to setup supported filter operations.
	 *
	 * @param registry filter operator registry
	 */
	protected abstract void setupFilterOperators(FilterOperatorRegistry registry);

	@Override
	public D findOneTarget(T_ID sourceId, String fieldName, QueryParams queryParams) {
		return findOneTarget(sourceId, fieldName, specParser.fromParams(getResourceClass(), queryParams));
	}

	protected abstract D findOneTarget(T_ID sourceId, String fieldName, QuerySpec querySpec);

	@Override
	public Iterable<D> findManyTargets(T_ID sourceId, String fieldName, QueryParams queryParams) {
		return findManyTargets(sourceId, fieldName, specParser.fromParams(getResourceClass(), queryParams));
	}

	protected abstract Iterable<D> findManyTargets(T_ID sourceId, String fieldName, QuerySpec querySpec);

}
