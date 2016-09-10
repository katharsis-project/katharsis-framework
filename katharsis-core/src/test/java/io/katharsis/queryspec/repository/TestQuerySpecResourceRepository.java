package io.katharsis.queryspec.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Assert;

import io.katharsis.queryspec.FilterOperator;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.QuerySpecResourceRepository;
import io.katharsis.queryspec.SortSpec;
import io.katharsis.resource.mock.models.Task;

public class TestQuerySpecResourceRepository implements QuerySpecResourceRepository<Task, Long> {

	private List<Task> tasks = new ArrayList<Task>();

	@Override
	public Class<Task> getResourceClass() {
		return Task.class;
	}

	private void assertQuerySpec(QuerySpec querySpec) {
		List<SortSpec> sorts = querySpec.getSort();
		Assert.assertEquals(1, sorts.size());
		SortSpec sort = sorts.get(0);
		Assert.assertEquals(Arrays.asList("name"), sort.getAttributePath());
	}

	@Override
	public Task findOne(Long id, QuerySpec querySpec) {
		assertQuerySpec(querySpec);
		for (Task task : tasks) {
			if (task.getId().equals(id)) {
				return task;
			}
		}
		return null;
	}

	@Override
	public Iterable<Task> findAll(QuerySpec querySpec) {
		assertQuerySpec(querySpec);
		return querySpec.apply(tasks);
	}

	@Override
	public Iterable<Task> findAll(Iterable<Long> ids, QuerySpec querySpec) {
		assertQuerySpec(querySpec);
		return querySpec.apply(tasks);
	}

	@Override
	public <S extends Task> S save(S entity) {
		tasks.add(entity);
		return null;
	}

	@Override
	public void delete(Long id) {
		Iterator<Task> iterator = tasks.iterator();
		while (iterator.hasNext()) {
			Task next = iterator.next();
			if (next.getId().equals(id)) {
				iterator.remove();
			}
		}
	}

	@Override
	public Set<FilterOperator> getSupportedOperators() {
		Set<FilterOperator> set = new HashSet<FilterOperator>();
		set.add(FilterOperator.EQ);
		set.add(FilterOperator.LE);
		set.add(FilterOperator.LT);
		set.add(FilterOperator.GE);
		set.add(FilterOperator.GT);
		return set;
	}

	@Override
	public FilterOperator getDefaultOperator() {
		return FilterOperator.EQ;
	}
}