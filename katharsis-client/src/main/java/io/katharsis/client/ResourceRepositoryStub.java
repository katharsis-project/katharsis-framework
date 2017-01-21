package io.katharsis.client;

import java.io.Serializable;
import java.util.List;

import io.katharsis.legacy.queryParams.QueryParams;
import io.katharsis.legacy.repository.ResourceRepository;

/**
 * Implemented by every {@link ResourceRepository} stub.
 */
public interface ResourceRepositoryStub<T, ID extends Serializable> extends ResourceRepository<T, ID> {

	@Override
	public List<T> findAll(QueryParams queryParams);

	@Override
	public List<T> findAll(Iterable<ID> ids, QueryParams queryParams);

	/**
	 * Saves the given entity without any of its relationships.
	 *
	 * @param entity resource to be saved
	 * @param <S> resource type
	 * @return persisted resource
	 */
	public <S extends T> S create(S entity);
}
