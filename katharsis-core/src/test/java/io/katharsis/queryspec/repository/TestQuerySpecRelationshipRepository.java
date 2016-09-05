package io.katharsis.queryspec.repository;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;

import io.katharsis.queryspec.FilterOperator;
import io.katharsis.queryspec.FilterOperatorRegistry;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.SortSpec;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;

public class TestQuerySpecRelationshipRepository extends QuerySpecRelationshipRepository<Task, Long, Project, Long> {

	public TestQuerySpecRelationshipRepository() {

	}

	@Override
	protected Class<Project> getResourceClass() {
		return Project.class;
	}

	private void assertQuerySpec(QuerySpec querySpec) {
		List<SortSpec> sorts = querySpec.getSort();
		Assert.assertEquals(1, sorts.size());
		SortSpec sort = sorts.get(0);
		Assert.assertEquals(Arrays.asList("name"), sort.getAttributePath());
	}

	@Override
	protected Project findOneTarget(Long sourceId, String fieldName, QuerySpec querySpec) {
		assertQuerySpec(querySpec);
		return null;
	}

	@Override
	protected Iterable<Project> findManyTargets(Long sourceId, String fieldName, QuerySpec querySpec) {
		assertQuerySpec(querySpec);
		return null;
	}

	@Override
	public void setRelation(Task source, Long targetId, String fieldName) {
	}

	@Override
	public void setRelations(Task source, Iterable<Long> targetIds, String fieldName) {
	}

	@Override
	public void addRelations(Task source, Iterable<Long> targetIds, String fieldName) {
	}

	@Override
	public void removeRelations(Task source, Iterable<Long> targetIds, String fieldName) {
	}

	@Override
	protected void setupFilterOperators(FilterOperatorRegistry registry) {
		registry.setDefaultOperator(FilterOperator.EQ);
	}
}