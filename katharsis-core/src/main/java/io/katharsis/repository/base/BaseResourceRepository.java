package io.katharsis.repository.base;

import java.io.Serializable;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.repository.base.query.DefaultQuerySpecParser;
import io.katharsis.repository.base.query.QuerySpec;
import io.katharsis.repository.base.query.QuerySpecParser;
import io.katharsis.resource.registry.ResourceRegistry;

/**
 * ResourceRepository base implementation preprocessing incoming QueryParams to
 * QuerySpec.
 */
public abstract class BaseResourceRepository<T, ID extends Serializable> implements ResourceRepository<T, ID> {

	private QuerySpecParser specParser;

	public BaseResourceRepository(ResourceRegistry resourceRegistry) {
		this.specParser = new DefaultQuerySpecParser(resourceRegistry);
	}

	@Override
	public final T findOne(ID id, QueryParams queryParams) {
		return findOne(id, specParser.fromParams(queryParams));
	}

	protected abstract T findOne(ID id, QuerySpec fromParams);

	@Override
	public Iterable<T> findAll(QueryParams queryParams) {
		return findAll(specParser.fromParams(queryParams));
	}

	protected abstract Iterable<T> findAll(QuerySpec fromParams);

	@Override
	public Iterable<T> findAll(Iterable<ID> ids, QueryParams queryParams) {
		return findAll(ids, specParser.fromParams(queryParams));
	}

	protected abstract Iterable<T> findAll(Iterable<ID> ids, QuerySpec fromParams);

	@Override
	public abstract <S extends T> S save(S entity);

	@Override
	public abstract void delete(ID id);

}
