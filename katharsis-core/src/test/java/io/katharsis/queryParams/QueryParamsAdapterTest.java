package io.katharsis.queryParams;

import io.katharsis.queryspec.internal.QueryParamsAdapter;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.registry.ConstantServiceUrlProvider;
import io.katharsis.resource.registry.ResourceRegistry;
import org.junit.Assert;
import org.junit.Test;

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

	@Test(expected = UnsupportedOperationException.class)
	public void testDuplicate() {
		QueryParams params = new QueryParams();
		QueryParamsAdapter adapter = new QueryParamsAdapter(params);
		adapter.duplicate();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGetPageAdapter() {
		QueryParams params = new QueryParams();
		QueryParamsAdapter adapter = new QueryParamsAdapter(params);
        adapter.getPageAdapter();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testSetPageAdapter() {
		QueryParams params = new QueryParams();
		QueryParamsAdapter adapter = new QueryParamsAdapter(params);
		adapter.setPageAdapter(null);
	}
}
