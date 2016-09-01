package io.katharsis.queryspec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.katharsis.resource.mock.models.Task;

public class InMemoryEvaluatorTest {

	private List<Task> tasks;

	@Before
	public void setup() {
		tasks = new ArrayList<>();
		for (long i = 0; i < 5; i++) {
			Task task = new Task();
			task.setId(i);
			task.setName("test" + i);
			tasks.add(task);
		}

	}

	@Test
	public void testAll() {
		QuerySpec spec = new QuerySpec(Task.class);
		Assert.assertEquals(5, spec.apply(tasks).size());
	}

	@Test
	public void setLimit() {
		QuerySpec spec = new QuerySpec(Task.class);
		spec.setLimit(2L);
		Assert.assertEquals(2, spec.apply(tasks).size());
	}
	
	@Test
	public void setOffset() {
		QuerySpec spec = new QuerySpec(Task.class);
		spec.setOffset(2L);
		Assert.assertEquals(3, spec.apply(tasks).size());
	}
	
	@Test
	public void setOffsetLimit() {
		QuerySpec spec = new QuerySpec(Task.class);
		spec.setOffset(2L);
		spec.setLimit(1L);
		List<Task> results = spec.apply(tasks);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(Long.valueOf(2L), results.get(0).getId());
	}
	
	@Test
	public void testSortAsc() {
		QuerySpec spec = new QuerySpec(Task.class);
		spec.addSort(new SortSpec(Arrays.asList("name"), Direction.ASC));
		List<Task> results = spec.apply(tasks);
		Assert.assertEquals(5, results.size());
		Assert.assertEquals("test0", results.get(0).getName());
	}

	@Test
	public void testSortDesc() {
		QuerySpec spec = new QuerySpec(Task.class);
		spec.addSort(new SortSpec(Arrays.asList("name"), Direction.DESC));
		List<Task> results = spec.apply(tasks);
		Assert.assertEquals(5, results.size());
		Assert.assertEquals("test4", results.get(0).getName());
	}

	@Test
	public void testFilterEquals() {
		QuerySpec spec = new QuerySpec(Task.class);
		spec.addFilter(new FilterSpec(Arrays.asList("name"), FilterOperator.EQ, "test1"));
		List<Task> results = spec.apply(tasks);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals("test1", results.get(0).getName());
	}

	@Test
	public void testFilterLE() {
		QuerySpec spec = new QuerySpec(Task.class);
		spec.addFilter(new FilterSpec(Arrays.asList("id"), FilterOperator.LE, 1L));
		List<Task> results = spec.apply(tasks);
		Assert.assertEquals(2, results.size());
	}
	
	@Test
	public void testFilterLT() {
		QuerySpec spec = new QuerySpec(Task.class);
		spec.addFilter(new FilterSpec(Arrays.asList("id"), FilterOperator.LT, 1L));
		List<Task> results = spec.apply(tasks);
		Assert.assertEquals(1, results.size());
	}
	
	@Test
	public void testFilterGE() {
		QuerySpec spec = new QuerySpec(Task.class);
		spec.addFilter(new FilterSpec(Arrays.asList("id"), FilterOperator.GE, 1L));
		List<Task> results = spec.apply(tasks);
		Assert.assertEquals(4, results.size());
	}
	
	@Test
	public void testFilterGT() {
		QuerySpec spec = new QuerySpec(Task.class);
		spec.addFilter(new FilterSpec(Arrays.asList("id"), FilterOperator.GT, 1L));
		List<Task> results = spec.apply(tasks);
		Assert.assertEquals(3, results.size());
	}
}
