package io.katharsis.queryspec.repository;

import java.io.Serializable;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryspec.DefaultQuerySpecConverter;
import io.katharsis.queryspec.FilterOperatorRegistry;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.QuerySpecConverter;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ResourceRegistryAware;

/**
 * QuerySpec-based ResourceRepository that translates incoming QueryParameters
 * to QuerySpec for ease of use.
 */
public abstract class QuerySpecResourceRepository<T, I extends Serializable>
		implements ResourceRegistryAware, ResourceRepository<T, I> {

	private QuerySpecConverter specParser;

	@Override
	public void setResourceRegistry(ResourceRegistry resourceRegistry) {
		FilterOperatorRegistry filterOperators = new FilterOperatorRegistry();
		setupFilterOperators(filterOperators);
		this.specParser = new DefaultQuerySpecConverter(resourceRegistry, filterOperators);
	}

	/**
	 * Use this method to setup supported filter operations.
	 */
	protected abstract void setupFilterOperators(FilterOperatorRegistry registry);

	protected abstract Class<T> getResourceClass();

	@Override
	public final T findOne(I id, QueryParams queryParams) {
		return findOne(id, specParser.fromParams(getResourceClass(), queryParams));
	}

	protected abstract T findOne(I id, QuerySpec querySpec);

	@Override
	public final Iterable<T> findAll(QueryParams queryParams) {
		return findAll(specParser.fromParams(getResourceClass(), queryParams));
	}

	protected abstract Iterable<T> findAll(QuerySpec querySpec);

	@Override
	public final Iterable<T> findAll(Iterable<I> ids, QueryParams queryParams) {
		return findAll(ids, specParser.fromParams(getResourceClass(), queryParams));
	}

	protected abstract Iterable<T> findAll(Iterable<I> ids, QuerySpec querySpec);

}
