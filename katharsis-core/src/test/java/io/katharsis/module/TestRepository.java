package io.katharsis.module;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.ResourceRepository;

class TestRepository implements ResourceRepository<TestResource, Integer> {

	@Override
	public TestResource findOne(Integer id, QueryParams queryParams) {
		return null;
	}

	@Override
	public Iterable<TestResource> findAll(QueryParams queryParams) {
		return null;
	}

	@Override
	public Iterable<TestResource> findAll(Iterable<Integer> ids, QueryParams queryParams) {
		return null;
	}

	@Override
	public <S extends TestResource> S save(S entity) {
		return null;
	}

	@Override
	public void delete(Integer id) {
	}
}
