package io.katharsis.client;

import java.io.Serializable;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.repository.ResourceRepositoryV2;
import io.katharsis.resource.list.ResourceList;

/**
 * Implemented by every {@link ResourceRepository} stub.
 */
public interface QuerySpecResourceRepositoryStub<T, I extends Serializable> extends ResourceRepositoryV2<T, I> {

	@Override
	public ResourceList<T> findAll(QuerySpec querySpec);

	@Override
	public ResourceList<T> findAll(Iterable<I> ids, QuerySpec querySpec);

	/**
	 * Saves the given entity with a PATCH request. Saves relations if they are loaded and either not-lazy or not-null.
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
	 * 
	 * @deprecated make use of {@link #save(S)} instead
	 */
	@Deprecated
	public <S extends T> S save(S entity, QuerySpec querySpec);

	/**
	 * Creates the given entity. Saves relations if they are loaded and either not-lazy or not-null.
	 *
	 * @param entity resource to be saved
	 * @param <S> resource type
	 * @return persisted resource
	 */
	@Override
	public <S extends T> S create(S entity);

	/**
	 * Creates the given entity. {@link QuerySpec} allows to specify which
	 * relationships should be saved (just the relation, not the related resource).
	 *
	 * @param entity resource to be saved
	 * @param querySpec querySpec
	 * @param <S> resource type
	 * @return persisted resource
	 * @deprecated make use of {@link #create(S)} instead
	 */
	@Deprecated
	public <S extends T> S create(S entity, QuerySpec querySpec);
}
