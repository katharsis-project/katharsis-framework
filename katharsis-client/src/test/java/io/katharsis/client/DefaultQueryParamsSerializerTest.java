package io.katharsis.client;

import com.squareup.okhttp.HttpUrl;
import io.katharsis.client.internal.RequestUrlBuilder;
import io.katharsis.client.mock.models.Task;
import io.katharsis.locator.JsonServiceLocator;
import io.katharsis.locator.SampleJsonServiceLocator;
import io.katharsis.queryParams.DefaultQueryParamsParser;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.resource.field.ResourceFieldNameTransformer;
import io.katharsis.resource.information.AnnotationResourceInformationBuilder;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.registry.ConstantServiceUrlProvider;
import io.katharsis.resource.registry.DefaultResourceLookup;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ResourceRegistryBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class DefaultQueryParamsSerializerTest {

	private QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());
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
		ResourceRegistry resourceRegistry = resourceRegistryBuilder.build(resourceLookup, new ConstantServiceUrlProvider("http://127.0.0.1"));
		urlBuilder = new RequestUrlBuilder(resourceRegistry);
	}

	@Test
	public void testHttpsSchema() {
		ResourceRegistry resourceRegistry = resourceRegistryBuilder.build(resourceLookup, new ConstantServiceUrlProvider("https://127.0.0.1"));
		urlBuilder = new RequestUrlBuilder(resourceRegistry);
		check("https://127.0.0.1/tasks/", null, new QueryParams());
	}
	
	@Test
	public void testPort() {
		ResourceRegistry resourceRegistry = resourceRegistryBuilder.build(resourceLookup, new ConstantServiceUrlProvider("https://127.0.0.1:1234"));
		urlBuilder = new RequestUrlBuilder(resourceRegistry);
		check("https://127.0.0.1:1234/tasks/", null, new QueryParams());
	}
	
	@Test
	public void testFindAll() throws InstantiationException, IllegalAccessException {
		check("http://127.0.0.1/tasks/", null, new QueryParams());
	}

	@Test
	public void testFindById() throws InstantiationException, IllegalAccessException {
		check("http://127.0.0.1/tasks/1/", 1, new QueryParams());
	}

	@Test
	public void testFindByIds() throws InstantiationException, IllegalAccessException {
		check("http://127.0.0.1/tasks/1,2,3/", Arrays.asList(1, 2, 3), new QueryParams());
	}

	@Test
	public void testFindAllOrderByAsc() throws InstantiationException, IllegalAccessException {
		testFindAllOrder(true);
	}

	@Test
	public void testFindAllOrderByDesc() throws InstantiationException, IllegalAccessException {
		testFindAllOrder(false);
	}

	public void testFindAllOrder(boolean asc) throws InstantiationException, IllegalAccessException {
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		String dir = asc ? "asc" : "desc";
		addParams(params, "sort[test][longValue]", dir);
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);
		check("http://127.0.0.1/tasks/?sort[test][longValue]=" + dir, null, queryParams);
	}

	@Test
	public void testFilterByOne() throws InstantiationException, IllegalAccessException {
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "filter[test][stringValue]", "value");
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);
		check("http://127.0.0.1/tasks/?filter[test][stringValue]=value", null, queryParams);
	}

	@Test
	public void testFilterByMany() throws InstantiationException, IllegalAccessException {
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "filter[test][stringValue]", "value0,value1");
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);
		check("http://127.0.0.1/tasks/?filter[test][stringValue]=value0,value1", null, queryParams);
	}

	@Test
	public void testFilterEquals() throws InstantiationException, IllegalAccessException {
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "filter[test][longValue][equal]", "1");
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);
		check("http://127.0.0.1/tasks/?filter[test][longValue][equal]=1", null, queryParams);
	}

	@Test
	public void testFilterGreater() throws InstantiationException, IllegalAccessException {
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "filter[test][longValue][greater]", "1");
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);
		check("http://127.0.0.1/tasks/?filter[test][longValue][greater]=1", null, queryParams);
	}

	@Test
	public void testFilterLike() throws InstantiationException, IllegalAccessException, UnsupportedEncodingException {
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "filter[test][longValue][like]", "test%");
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);
		check("http://127.0.0.1/tasks/?filter[test][longValue][like]=" + URLEncoder.encode("test%", "UTF-8"), null,
				queryParams);
	}

	//
	@Test
	public void testPaging() throws InstantiationException, IllegalAccessException {
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "page[offset]", "1");
		addParams(params, "page[limit]", "2");
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);

		check("http://127.0.0.1/tasks/?page[limit]=2&page[offset]=1", null, queryParams);
	}

	@Test
	public void testIncludeRelations() throws InstantiationException, IllegalAccessException {
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "include[test]", "project");
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);

		check("http://127.0.0.1/tasks/?include[test]=project", null, queryParams);
	}

	@Test
	public void testIncludeAttributes() throws InstantiationException, IllegalAccessException {
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "fields[test]", "project");
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);

		check("http://127.0.0.1/tasks/?fields[test]=project", null, queryParams);
	}

	private void check(String expectedUrl, Object id, QueryParams queryParams) {
		HttpUrl actualUrl = urlBuilder.buildUrl(Task.class, id, queryParams);
		HttpUrl expectedUrlObj = HttpUrl.parse(expectedUrl);
		assertEquals(expectedUrlObj, actualUrl);
	}

	private void addParams(Map<String, Set<String>> params, String key, String value) {
		params.put(key, new HashSet<>(Collections.singletonList(value)));
	}
}
