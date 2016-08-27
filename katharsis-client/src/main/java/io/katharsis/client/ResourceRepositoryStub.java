package io.katharsis.client;

import java.io.Serializable;
import java.util.List;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.ResourceRepository;

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
	 */
	@Override
	public <S extends T> S save(S entity);

	/**
	 * Saves the given entity. {@link QueryParams} allows to specify which
	 * relationships should be saved as well.
	 */
	public <S extends T> S save(S entity, QueryParams queryParams);
}
