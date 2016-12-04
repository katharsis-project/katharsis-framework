package io.katharsis.client;

import java.io.Serializable;

import io.katharsis.repository.ResourceRepository;
import io.katharsis.repository.ResourceRepositoryV2;

/**
 * Implemented by every {@link ResourceRepository} stub.
 */
public interface QuerySpecResourceRepositoryStub<T, I extends Serializable> extends ResourceRepositoryV2<T, I> {

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
	 * Creates the given entity. Saves relations if they are loaded and either not-lazy or not-null.
	 *
	 * @param entity resource to be saved
	 * @param <S> resource type
	 * @return persisted resource
	 */
	@Override
	public <S extends T> S create(S entity);

}
