package io.katharsis.client;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.squareup.okhttp.HttpUrl;

import io.katharsis.client.internal.RequestUrlBuilder;
import io.katharsis.client.mock.models.Task;
import io.katharsis.locator.JsonServiceLocator;
import io.katharsis.locator.SampleJsonServiceLocator;
import io.katharsis.queryspec.Direction;
import io.katharsis.queryspec.FilterOperator;
import io.katharsis.queryspec.FilterSpec;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.SortSpec;
import io.katharsis.resource.field.ResourceFieldNameTransformer;
import io.katharsis.resource.information.AnnotationResourceInformationBuilder;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.registry.ConstantServiceUrlProvider;
import io.katharsis.resource.registry.DefaultResourceLookup;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ResourceRegistryBuilder;

public class DefaultQuerySpecSerializerTest {

	private RequestUrlBuilder urlBuilder;

	private DefaultResourceLookup resourceLookup;

	private ResourceRegistryBuilder resourceRegistryBuilder;

	@Before
	public void setup() {
		JsonServiceLocator jsonServiceLocator = new SampleJsonServiceLocator();
		ResourceInformationBuilder resourceInformationBuilder = new AnnotationResourceInformationBuilder(
				new ResourceFieldNameTransformer());
		resourceRegistryBuilder = new ResourceRegistryBuilder(jsonServiceLocator, resourceInformationBuilder);
		resourceLookup = new DefaultResourceLookup("io.katharsis.client.mock");
		ResourceRegistry resourceRegistry = resourceRegistryBuilder.build(resourceLookup,
				new ConstantServiceUrlProvider("http://127.0.0.1"));
		urlBuilder = new RequestUrlBuilder(resourceRegistry);
	}

	@Test
	public void testHttpsSchema() {
		ResourceRegistry resourceRegistry = resourceRegistryBuilder.build(resourceLookup,
				new ConstantServiceUrlProvider("https://127.0.0.1"));
		urlBuilder = new RequestUrlBuilder(resourceRegistry);
		check("https://127.0.0.1/tasks/", null, new QuerySpec(Task.class));
	}

	@Test
	public void testPort() {
		ResourceRegistry resourceRegistry = resourceRegistryBuilder.build(resourceLookup,
				new ConstantServiceUrlProvider("https://127.0.0.1:1234"));
		urlBuilder = new RequestUrlBuilder(resourceRegistry);
		check("https://127.0.0.1:1234/tasks/", null, new QuerySpec(Task.class));
	}

	@Test
	public void testFindAll() throws InstantiationException, IllegalAccessException {
		check("http://127.0.0.1/tasks/", null, new QuerySpec(Task.class));
	}

	@Test
	public void testFindById() throws InstantiationException, IllegalAccessException {
		check("http://127.0.0.1/tasks/1/", 1, new QuerySpec(Task.class));
	}

	@Test
	public void testFindByIds() throws InstantiationException, IllegalAccessException {
		check("http://127.0.0.1/tasks/1,2,3/", Arrays.asList(1, 2, 3), new QuerySpec(Task.class));
	}

	@Test
	public void testFindAllOrderByAsc() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.addSort(new SortSpec(Arrays.asList("name"), Direction.ASC));
		check("http://127.0.0.1/tasks/?sort[tasks]=name", null, querySpec);
	}

	@Test
	public void testFindAllOrderByDesc() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.addSort(new SortSpec(Arrays.asList("name"), Direction.DESC));
		check("http://127.0.0.1/tasks/?sort[tasks]=-name", null, querySpec);
	}

	@Test
	public void testFilterByOne() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("name"), FilterOperator.EQ, "value"));
		check("http://127.0.0.1/tasks/?filter[tasks][name][EQ]=value", null, querySpec);
	}

	@Test
	public void testFilterByMany() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("name"), FilterOperator.EQ, Arrays.asList("value1", "value2")));
		check("http://127.0.0.1/tasks/?filter[tasks][name][EQ]=value2&filter[tasks][name][EQ]=value1", null, querySpec);
	}

	@Test
	public void testFilterEquals() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("id"), FilterOperator.EQ, 1));
		check("http://127.0.0.1/tasks/?filter[tasks][id][EQ]=1", null, querySpec);
	}

	@Test
	public void testFilterGreater() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("id"), FilterOperator.LE, 1));
		check("http://127.0.0.1/tasks/?filter[tasks][id][LE]=1", null, querySpec);
	}

	//
	@Test
	public void testPaging() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.setLimit(2L);
		querySpec.setOffset(1L);
		check("http://127.0.0.1/tasks/?page[limit]=2&page[offset]=1", null, querySpec);
	}

	@Test
	public void testIncludeRelations() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.includeRelation(Arrays.asList("project"));
		check("http://127.0.0.1/tasks/?include[tasks]=project", null, querySpec);
	}

	@Test
	public void testIncludeAttributes() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.includeField(Arrays.asList("name"));
		check("http://127.0.0.1/tasks/?fields[tasks]=name", null, querySpec);
	}

	private void check(String expectedUrl, Object id, QuerySpec querySpec) {
		HttpUrl actualUrl = urlBuilder.buildUrl(Task.class, id, querySpec);
		HttpUrl expectedUrlObj = HttpUrl.parse(expectedUrl);
		assertEquals(expectedUrlObj, actualUrl);
	}
}
