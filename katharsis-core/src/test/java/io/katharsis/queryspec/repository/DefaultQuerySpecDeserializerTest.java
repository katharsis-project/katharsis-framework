package io.katharsis.queryspec.repository;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import io.katharsis.core.internal.utils.PropertyException;
import io.katharsis.errorhandling.exception.BadRequestException;
import io.katharsis.errorhandling.exception.ParametersDeserializationException;
import io.katharsis.queryspec.AbstractQuerySpecTest;
import io.katharsis.queryspec.DefaultQuerySpecDeserializer;
import io.katharsis.queryspec.Direction;
import io.katharsis.queryspec.FilterOperator;
import io.katharsis.queryspec.FilterSpec;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.QuerySpecDeserializerContext;
import io.katharsis.queryspec.SortSpec;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.models.TaskWithLookup;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.utils.parser.TypeParser;

public class DefaultQuerySpecDeserializerTest extends AbstractQuerySpecTest {

	private DefaultQuerySpecDeserializer deserializer;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private ResourceInformation taskInformation;

	@Before
	public void setup() {
		super.setup();
		deserializer = new DefaultQuerySpecDeserializer();
		deserializer.init(new QuerySpecDeserializerContext() {

			@Override
			public ResourceRegistry getResourceRegistry() {
				return resourceRegistry;
			}

			@Override
			public TypeParser getTypeParser() {
				return moduleRegistry.getTypeParser();
			}
		});
		taskInformation = resourceRegistry.getEntryForClass(Task.class).getResourceInformation();
	}

	@Test
	public void operations() {
		Assert.assertFalse(deserializer.getAllowUnknownAttributes());
		deserializer.setAllowUnknownAttributes(true);
		Assert.assertTrue(deserializer.getAllowUnknownAttributes());
		Assert.assertEquals(0, deserializer.getDefaultOffset());
		Assert.assertNull(deserializer.getDefaultLimit());
		Assert.assertNull(deserializer.getMaxPageLimit());
		deserializer.getSupportedOperators().clear();
		deserializer.setDefaultOperator(FilterOperator.LIKE);
		deserializer.addSupportedOperator(FilterOperator.LIKE);
		Assert.assertEquals(FilterOperator.LIKE, deserializer.getDefaultOperator());
		Assert.assertEquals(1, deserializer.getSupportedOperators().size());
	}

	@Test
	public void testFindAll() throws InstantiationException, IllegalAccessException {
		Map<String, Set<String>> params = new HashMap<>();

		QuerySpec actualSpec = deserializer.deserialize(taskInformation, params);
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test
	public void defaultPaginationOnRoot() {
		Map<String, Set<String>> params = new HashMap<>();
		deserializer.setDefaultLimit(12L);
		deserializer.setDefaultOffset(1L);
		QuerySpec actualSpec = deserializer.deserialize(taskInformation, params);
		Assert.assertEquals(1L, actualSpec.getOffset());
		Assert.assertEquals(12L, actualSpec.getLimit().longValue());
	}

	@Test
	public void defaultPaginationOnRelation() {
		Map<String, Set<String>> params = new HashMap<>();
		add(params, "sort[projects]", "name");
		deserializer.setDefaultLimit(12L);
		deserializer.setDefaultOffset(1L);
		QuerySpec actualSpec = deserializer.deserialize(taskInformation, params);
		Assert.assertEquals(1L, actualSpec.getOffset());
		Assert.assertEquals(12L, actualSpec.getLimit().longValue());
		QuerySpec projectQuerySpec = actualSpec.getQuerySpec(Project.class);
		Assert.assertNotNull(projectQuerySpec);
		Assert.assertEquals(1L, projectQuerySpec.getOffset());
		Assert.assertEquals(12L, projectQuerySpec.getLimit().longValue());
	}

	@Test
	public void testFindAllOrderByAsc() throws InstantiationException, IllegalAccessException {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.addSort(new SortSpec(Arrays.asList("name"), Direction.ASC));

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "sort[tasks]", "name");
		QuerySpec actualSpec = deserializer.deserialize(taskInformation, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test
	public void testOrderByMultipleAttributes() throws InstantiationException, IllegalAccessException {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.addSort(new SortSpec(Arrays.asList("name"), Direction.ASC));
		expectedSpec.addSort(new SortSpec(Arrays.asList("id"), Direction.ASC));

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "sort[tasks]", "name,id");
		QuerySpec actualSpec = deserializer.deserialize(taskInformation, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test
	public void testFindAllOrderByDesc() throws InstantiationException, IllegalAccessException {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.addSort(new SortSpec(Arrays.asList("name"), Direction.DESC));

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "sort[tasks]", "-name");

		QuerySpec actualSpec = deserializer.deserialize(taskInformation, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test
	public void testFilterWithDefaultOp() throws InstantiationException, IllegalAccessException {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.addFilter(new FilterSpec(Arrays.asList("name"), FilterOperator.EQ, "value"));

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "filter[tasks][name]", "value");

		QuerySpec actualSpec = deserializer.deserialize(taskInformation, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test
	public void testFilterWithDotNotation() throws InstantiationException, IllegalAccessException {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.addFilter(new FilterSpec(Arrays.asList("project", "name"), FilterOperator.EQ, "value"));

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "filter[project.name]", "value");

		QuerySpec actualSpec = deserializer.deserialize(taskInformation, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test
	public void testFilterWithDotNotationMultipleElements() throws InstantiationException, IllegalAccessException {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.addFilter(new FilterSpec(Arrays.asList("project", "task", "name"), FilterOperator.EQ, "value"));

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "filter[project.task.name]", "value");

		QuerySpec actualSpec = deserializer.deserialize(taskInformation, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test
	public void testUnknownPropertyAllowed() throws InstantiationException, IllegalAccessException {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.addFilter(new FilterSpec(Arrays.asList("doesNotExists"), FilterOperator.EQ, "value"));

		deserializer.setAllowUnknownAttributes(true);

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "filter[tasks][doesNotExists]", "value");

		QuerySpec actualSpec = deserializer.deserialize(taskInformation, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test(expected = PropertyException.class)
	public void testUnknownPropertyNotAllowed() throws InstantiationException, IllegalAccessException {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.addFilter(new FilterSpec(Arrays.asList("doesNotExists"), FilterOperator.EQ, "value"));

		deserializer.setAllowUnknownAttributes(false);

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "filter[tasks][doesNotExists]", "value");

		deserializer.deserialize(taskInformation, params);
	}

	@Test
	public void testFilterByOne() throws InstantiationException, IllegalAccessException {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.addFilter(new FilterSpec(Arrays.asList("name"), FilterOperator.EQ, "value"));

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "filter[tasks][name][EQ]", "value");

		QuerySpec actualSpec = deserializer.deserialize(taskInformation, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test
	public void testFilterByMany() throws InstantiationException, IllegalAccessException {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.addFilter(new FilterSpec(Arrays.asList("name"), FilterOperator.EQ, new HashSet<>(Arrays.asList("value1", "value2"))));

		Map<String, Set<String>> params = new HashMap<>();
		params.put("filter[tasks][name][EQ]", new HashSet<>(Arrays.asList("value1", "value2")));

		QuerySpec actualSpec = deserializer.deserialize(taskInformation, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test
	public void testFilterEquals() throws InstantiationException, IllegalAccessException {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.addFilter(new FilterSpec(Arrays.asList("id"), FilterOperator.EQ, 1L));

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "filter[tasks][id][EQ]", "1");

		QuerySpec actualSpec = deserializer.deserialize(taskInformation, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test
	public void testFilterGreater() throws InstantiationException, IllegalAccessException {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.addFilter(new FilterSpec(Arrays.asList("id"), FilterOperator.LE, 1L));

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "filter[tasks][id][LE]", "1");

		QuerySpec actualSpec = deserializer.deserialize(taskInformation, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test
	public void testFilterGreaterOnRoot() throws InstantiationException, IllegalAccessException {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.addFilter(new FilterSpec(Arrays.asList("id"), FilterOperator.LE, 1L));

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "filter[id][LE]", "1");

		QuerySpec actualSpec = deserializer.deserialize(taskInformation, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test
	public void testPaging() throws InstantiationException, IllegalAccessException {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.setLimit(2L);
		expectedSpec.setOffset(1L);

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "page[offset]", "1");
		add(params, "page[limit]", "2");

		QuerySpec actualSpec = deserializer.deserialize(taskInformation, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test(expected = ParametersDeserializationException.class)
	public void testPagingError() throws InstantiationException, IllegalAccessException {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.setLimit(2L);
		expectedSpec.setOffset(1L);

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "page[offset]", "notANumber");
		add(params, "page[limit]", "2");

		deserializer.deserialize(taskInformation, params);
	}

	@Test
	public void testPagingMaxLimitNotAllowed() throws InstantiationException, IllegalAccessException {
		Map<String, Set<String>> params = new HashMap<>();
		add(params, "page[offset]", "1");
		add(params, "page[limit]", "5");

		deserializer.setMaxPageLimit(3L);
		expectedException.expect(BadRequestException.class);

		deserializer.deserialize(taskInformation, params);
	}

	@Test
	public void testPagingMaxLimitAllowed() throws InstantiationException, IllegalAccessException {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.setOffset(1L);
		expectedSpec.setLimit(5L);

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "page[offset]", "1");
		add(params, "page[limit]", "5");

		deserializer.setMaxPageLimit(5L);
		QuerySpec actualSpec = deserializer.deserialize(taskInformation, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test
	public void testIncludeRelations() throws InstantiationException, IllegalAccessException {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.includeRelation(Arrays.asList("project"));

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "include[tasks]", "project");

		QuerySpec actualSpec = deserializer.deserialize(taskInformation, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test
	public void testIncludeRelationsOnRoot() throws InstantiationException, IllegalAccessException {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.includeRelation(Arrays.asList("project"));

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "include", "project");

		QuerySpec actualSpec = deserializer.deserialize(taskInformation, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test
	public void testIncludeAttributes() throws InstantiationException, IllegalAccessException {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.includeField(Arrays.asList("name"));

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "fields[tasks]", "name");

		QuerySpec actualSpec = deserializer.deserialize(taskInformation, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test
	public void testIncludeAttributesOnRoot() throws InstantiationException, IllegalAccessException {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.includeField(Arrays.asList("name"));

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "fields", "name");

		QuerySpec actualSpec = deserializer.deserialize(taskInformation, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test
	public void testHyphenIsAllowedInResourceName(){

		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.addSort(new SortSpec(Arrays.asList("id"), Direction.ASC));

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "sort[task-with-lookup]", "id");

		ResourceInformation taskWithLookUpInformation = resourceRegistry.getEntryForClass(TaskWithLookup.class).getResourceInformation();
		QuerySpec actualSpec = deserializer.deserialize(taskWithLookUpInformation, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	private void add(Map<String, Set<String>> params, String key, String value) {
		params.put(key, new HashSet<>(Arrays.asList(value)));
	}
}
