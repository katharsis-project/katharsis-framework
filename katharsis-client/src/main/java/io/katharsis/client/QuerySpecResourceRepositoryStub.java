package io.katharsis.client;

import java.io.Serializable;
import java.util.List;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.QuerySpecResourceRepository;
import io.katharsis.repository.ResourceRepository;

/**
 * Implemented by every {@link ResourceRepository} stub.
 */
public interface QuerySpecResourceRepositoryStub<T, ID extends Serializable> extends QuerySpecResourceRepository<T, ID> {

	@Override
	public List<T> findAll(QuerySpec querySpec);

	@Override
	public List<T> findAll(Iterable<ID> ids, QuerySpec querySpec);

	/**
	 * Saves the given entity without any of its relationships.
	 *
	 * @param entity resource to be saved
	 * @param <S> resource type
	 * @return persisted resource
	 */
	@Override
	public <S extends T> S save(S entity);

	/**
	 * Saves the given entity. {@link QueryParams} allows to specify which
	 * relationships should be saved as well.
	 *
	 * @param entity resource to be saved
	 * @param querySpec querySpec
	 * @param <S> resource type
	 * @return persisted resource
	 */
	public <S extends T> S save(S entity, QuerySpec querySpec);
}
