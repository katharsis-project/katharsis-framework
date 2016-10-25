package io.katharsis.client;

import java.io.Serializable;

import io.katharsis.client.response.ResourceList;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.QuerySpecResourceRepository;
import io.katharsis.repository.ResourceRepository;

/**
 * Implemented by every {@link ResourceRepository} stub.
 */
public interface QuerySpecResourceRepositoryStub<T, I extends Serializable> extends QuerySpecResourceRepository<T, I> {

	@Override
	public ResourceList<T> findAll(QuerySpec querySpec);

	@Override
	public ResourceList<T> findAll(Iterable<I> ids, QuerySpec querySpec);

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
	
	
	/**
	 * Creates the given entity without any of its relationships.
	 *
	 * @param entity resource to be saved
	 * @param <S> resource type
	 * @return persisted resource
	 */
	public <S extends T> S create(S entity);

	/**
	 * Creates the given entity. {@link QueryParams} allows to specify which
	 * relationships should be saved as well (just the relation, not the related resource).
	 *
	 * @param entity resource to be saved
	 * @param querySpec querySpec
	 * @param <S> resource type
	 * @return persisted resource
	 */
	public <S extends T> S create(S entity, QuerySpec querySpec);
}
