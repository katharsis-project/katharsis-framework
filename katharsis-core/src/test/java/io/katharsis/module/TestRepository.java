package io.katharsis.module;

import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.QuerySpecResourceRepository;

class TestRepository implements QuerySpecResourceRepository<TestResource, Integer> {

	@Override
	public <S extends TestResource> S save(S entity) {
		return null;
	}

	@Override
	public void delete(Integer id) {
	}

	@Override
	public Class<TestResource> getResourceClass() {
		return TestResource.class;
	}

	@Override
	public TestResource findOne(Integer id, QuerySpec querySpec) {
		return null;
	}

	@Override
	public Iterable<TestResource> findAll(QuerySpec querySpec) {
		return null;
	}

	@Override
	public Iterable<TestResource> findAll(Iterable<Integer> ids, QuerySpec querySpec) {
		return null;
	}
}
