package io.katharsis.legacy.queryParams;

import org.junit.Assert;
import org.junit.Test;

import io.katharsis.legacy.internal.QueryParamsAdapter;
import io.katharsis.legacy.queryParams.QueryParams;
import io.katharsis.module.ModuleRegistry;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.registry.ConstantServiceUrlProvider;
import io.katharsis.resource.registry.ResourceRegistry;

public class QueryParamsAdapterTest {

	@Test
	public void test() {
		ResourceRegistry resourceRegistry = new ResourceRegistry(new ModuleRegistry(), new ConstantServiceUrlProvider("http://localhost"));
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
	public void testGetLimit() {
		QueryParams params = new QueryParams();
		QueryParamsAdapter adapter = new QueryParamsAdapter(params);
		adapter.getLimit();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGetOffset() {
		QueryParams params = new QueryParams();
		QueryParamsAdapter adapter = new QueryParamsAdapter(params);
		adapter.getOffset();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testSetLimit() {
		QueryParams params = new QueryParams();
		QueryParamsAdapter adapter = new QueryParamsAdapter(params);
		adapter.setLimit(0L);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testSetOffset() {
		QueryParams params = new QueryParams();
		QueryParamsAdapter adapter = new QueryParamsAdapter(params);
		adapter.setOffset(0L);
	}
}
