package io.katharsis.queryspec.repository;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;

import io.katharsis.queryspec.FilterOperator;
import io.katharsis.queryspec.FilterOperatorRegistry;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.SortSpec;
import io.katharsis.resource.mock.models.Task;

public class TestQuerySpecResourceRepository extends QuerySpecResourceRepository<Task, Long> {

	@Override
	protected Class<Task> getResourceClass() {
		return Task.class;
	}

	private void assertQuerySpec(QuerySpec querySpec) {
		List<SortSpec> sorts = querySpec.getSort();
		Assert.assertEquals(1, sorts.size());
		SortSpec sort = sorts.get(0);
		Assert.assertEquals(Arrays.asList("name"), sort.getAttributePath());
	}

	@Override
	protected Task findOne(Long id, QuerySpec querySpec) {
		assertQuerySpec(querySpec);
		return null;
	}

	@Override
	protected Iterable<Task> findAll(QuerySpec querySpec) {
		assertQuerySpec(querySpec);
		return null;
	}

	@Override
	protected Iterable<Task> findAll(Iterable<Long> ids, QuerySpec querySpec) {
		assertQuerySpec(querySpec);
		return null;
	}

	@Override
	public <S extends Task> S save(S entity) {
		return null;
	}

	@Override
	public void delete(Long id) {
	}

	@Override
	protected void setupFilterOperators(FilterOperatorRegistry registry) {
		registry.setDefaultOperator(FilterOperator.EQ);
	}
}