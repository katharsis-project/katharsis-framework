package io.katharsis.queryspec.repository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;

import io.katharsis.queryspec.FilterOperator;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.QuerySpecRelationshipRepository;
import io.katharsis.queryspec.SortSpec;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;

public class TestQuerySpecRelationshipRepository implements QuerySpecRelationshipRepository<Task, Long, Project, Long> {

	public TestQuerySpecRelationshipRepository() {

	}

	@Override
	public Class<Task> getSourceResourceClass() {
		return Task.class;
	}

	@Override
	public Class<Project> getTargetResourceClass() {
		return Project.class;
	}

	private void assertQuerySpec(QuerySpec querySpec) {
		List<SortSpec> sorts = querySpec.getSort();
		Assert.assertEquals(1, sorts.size());
		SortSpec sort = sorts.get(0);
		Assert.assertEquals(Arrays.asList("name"), sort.getAttributePath());
	}

	@Override
	public Project findOneTarget(Long sourceId, String fieldName, QuerySpec querySpec) {
		assertQuerySpec(querySpec);
		return null;
	}

	@Override
	public Iterable<Project> findManyTargets(Long sourceId, String fieldName, QuerySpec querySpec) {
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
	public Set<FilterOperator> getSupportedOperators() {
		Set<FilterOperator> set = new HashSet<FilterOperator>();
		set.add(FilterOperator.EQ);
		return set;
	}

	@Override
	public FilterOperator getDefaultOperator() {
		return FilterOperator.EQ;
	}
}