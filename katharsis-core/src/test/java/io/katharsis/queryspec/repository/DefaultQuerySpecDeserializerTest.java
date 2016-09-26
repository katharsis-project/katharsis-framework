package io.katharsis.queryspec.repository;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.katharsis.queryspec.AbstractQuerySpecTest;
import io.katharsis.queryspec.DefaultQuerySpecDeserializer;
import io.katharsis.queryspec.Direction;
import io.katharsis.queryspec.FilterOperator;
import io.katharsis.queryspec.FilterSpec;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.QuerySpecDeserializerContext;
import io.katharsis.queryspec.SortSpec;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.registry.ResourceRegistry;

public class DefaultQuerySpecDeserializerTest extends AbstractQuerySpecTest {

	private DefaultQuerySpecDeserializer deserializer;

	@Before
	public void setup() {
		super.setup();
		deserializer = new DefaultQuerySpecDeserializer();
		deserializer.init(new QuerySpecDeserializerContext() {

			@Override
			public ResourceRegistry getResourceRegistry() {
				return resourceRegistry;
			}
		});
	}

	@Test
	public void operations() {
		deserializer.getSupportedOperators().clear();
		deserializer.setDefaultOperator(FilterOperator.LIKE);
		deserializer.addSupportedOperator(FilterOperator.LIKE);
		Assert.assertEquals(FilterOperator.LIKE, deserializer.getDefaultOperator());
		Assert.assertEquals(1, deserializer.getSupportedOperators().size());
	}

	@Test
	public void testFindAll() throws InstantiationException, IllegalAccessException {
		Map<String, Set<String>> params = new HashMap<>();

		QuerySpec actualSpec = deserializer.deserialize(Task.class, params);
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test
	public void testFindAllOrderByAsc() throws InstantiationException, IllegalAccessException {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.addSort(new SortSpec(Arrays.asList("name"), Direction.ASC));

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "sort[tasks]", "name");
		QuerySpec actualSpec = deserializer.deserialize(Task.class, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test
	public void testFindAllOrderByDesc() throws InstantiationException, IllegalAccessException {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.addSort(new SortSpec(Arrays.asList("name"), Direction.DESC));

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "sort[tasks]", "-name");

		QuerySpec actualSpec = deserializer.deserialize(Task.class, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test
	public void testFilterByOne() throws InstantiationException, IllegalAccessException {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.addFilter(new FilterSpec(Arrays.asList("name"), FilterOperator.EQ, "value"));

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "filter[tasks][name][EQ]", "value");

		QuerySpec actualSpec = deserializer.deserialize(Task.class, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test
	public void testFilterByMany() throws InstantiationException, IllegalAccessException {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.addFilter(
				new FilterSpec(Arrays.asList("name"), FilterOperator.EQ, new HashSet<>(Arrays.asList("value1", "value2"))));

		Map<String, Set<String>> params = new HashMap<>();
		params.put("filter[tasks][name][EQ]", new HashSet<>(Arrays.asList("value1", "value2")));

		QuerySpec actualSpec = deserializer.deserialize(Task.class, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test
	public void testFilterEquals() throws InstantiationException, IllegalAccessException {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.addFilter(new FilterSpec(Arrays.asList("id"), FilterOperator.EQ, 1L));

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "filter[tasks][id][EQ]", "1");

		QuerySpec actualSpec = deserializer.deserialize(Task.class, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test
	public void testFilterGreater() throws InstantiationException, IllegalAccessException {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.addFilter(new FilterSpec(Arrays.asList("id"), FilterOperator.LE, 1L));

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "filter[tasks][id][LE]", "1");

		QuerySpec actualSpec = deserializer.deserialize(Task.class, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	//
	@Test
	public void testPaging() throws InstantiationException, IllegalAccessException {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.setLimit(2L);
		expectedSpec.setOffset(1L);

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "page[offset]", "1");
		add(params, "page[limit]", "2");

		QuerySpec actualSpec = deserializer.deserialize(Task.class, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test
	public void testIncludeRelations() throws InstantiationException, IllegalAccessException {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.includeRelation(Arrays.asList("project"));

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "include[tasks]", "project");

		QuerySpec actualSpec = deserializer.deserialize(Task.class, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test
	public void testIncludeAttributes() throws InstantiationException, IllegalAccessException {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.includeField(Arrays.asList("name"));

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "fields[tasks]", "name");

		QuerySpec actualSpec = deserializer.deserialize(Task.class, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	private void add(Map<String, Set<String>> params, String key, String value) {
		params.put(key, new HashSet<>(Arrays.asList(value)));
	}
}
