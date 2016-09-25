package io.katharsis.queryParams;

import org.junit.Assert;
import org.junit.Test;

import io.katharsis.queryspec.internal.QueryParamsAdapter;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.registry.ConstantServiceUrlProvider;
import io.katharsis.resource.registry.ResourceRegistry;

public class QueryParamsAdapterTest {

	@Test
	public void test() {
		ResourceRegistry resourceRegistry = new ResourceRegistry(new ConstantServiceUrlProvider("http://localhost"));
		QueryParams params = new QueryParams();
		QueryParamsAdapter adapter = new QueryParamsAdapter(Task.class, params, resourceRegistry);
		Assert.assertEquals(Task.class, adapter.getResourceClass());
		Assert.assertEquals(resourceRegistry, adapter.getResourceRegistry());
	}

	@Test(expected = IllegalStateException.class)
	public void testGetNonExistingResourceClass() {
		QueryParams params = new QueryParams();
		QueryParamsAdapter adapter = new QueryParamsAdapter(params);
		adapter.getResourceClass();
	}

	@Test(expected = IllegalStateException.class)
	public void testGetNonExistingRegistry() {
		QueryParams params = new QueryParams();
		QueryParamsAdapter adapter = new QueryParamsAdapter(params);
		adapter.getResourceRegistry();
	}
}
