package io.katharsis.module;

import io.katharsis.queryspec.QuerySpec;
import io.katharsis.repository.ResourceRepositoryV2;
import io.katharsis.resource.list.ResourceList;

class TestRepository implements ResourceRepositoryV2<TestResource, Integer> {

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
	public ResourceList<TestResource> findAll(QuerySpec querySpec) {
		return null;
	}

	@Override
	public ResourceList<TestResource> findAll(Iterable<Integer> ids, QuerySpec querySpec) {
		return null;
	}

	@Override
	public <S extends TestResource> S create(S entity) {
		return null;
	}
}
