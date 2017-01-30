package io.katharsis.legacy.repository;

import java.io.Serializable;

import io.katharsis.legacy.queryParams.QueryParams;

public abstract class AbstractSimpleRepository<T, ID extends Serializable> implements ResourceRepository<T, ID> {

	public T findOne(ID id) {
		throw new UnsupportedOperationException("findOne not implemented");
	}

	@Override
	public T findOne(ID id, QueryParams queryParams) {
		return findOne(id);
	}

	@Override
	public Iterable<T> findAll(QueryParams queryParams) {
		throw new UnsupportedOperationException("findAll not implemented");
	}

	@Override
	public Iterable<T> findAll(Iterable<ID> ids, QueryParams queryParams) {
		throw new UnsupportedOperationException("findAll not supported");
	}

	@Override
	public <S extends T> S save(S entity) {
		throw new UnsupportedOperationException("save not supported on ");

	}

	@Override
	public void delete(ID id) {
		throw new UnsupportedOperationException("delete not supported on ");
	}

}