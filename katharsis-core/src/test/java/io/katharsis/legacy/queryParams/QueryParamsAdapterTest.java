package io.katharsis.legacy.queryParams;

import org.junit.Assert;
import org.junit.Test;

import io.katharsis.core.internal.registry.ResourceRegistryImpl;
import io.katharsis.core.internal.resource.AnnotationResourceInformationBuilder;
import io.katharsis.legacy.internal.QueryParamsAdapter;
import io.katharsis.legacy.registry.DefaultResourceInformationBuilderContext;
import io.katharsis.module.ModuleRegistry;
import io.katharsis.resource.information.ResourceFieldNameTransformer;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.registry.ConstantServiceUrlProvider;
import io.katharsis.resource.registry.ResourceRegistry;

public class QueryParamsAdapterTest {

	@Test
	public void test() {
		ModuleRegistry moduleRegistry = new ModuleRegistry();
		ResourceRegistry resourceRegistry = new ResourceRegistryImpl(moduleRegistry, new ConstantServiceUrlProvider("http://localhost"));
		QueryParams params = new QueryParams();
		
		AnnotationResourceInformationBuilder builder = new AnnotationResourceInformationBuilder(new ResourceFieldNameTransformer());
		builder.init(new DefaultResourceInformationBuilderContext(builder, moduleRegistry.getTypeParser()));
		ResourceInformation info = builder.build(Task.class);
		
		QueryParamsAdapter adapter = new QueryParamsAdapter(info, params, resourceRegistry);
		Assert.assertEquals(Task.class, adapter.getResourceInformation().getResourceClass());
		Assert.assertEquals(resourceRegistry, adapter.getResourceRegistry());
	}

	@Test(expected = IllegalStateException.class)
	public void testGetNonExistingResourceClass() {
		QueryParams params = new QueryParams();
		QueryParamsAdapter adapter = new QueryParamsAdapter(params);
		adapter.getResourceInformation();
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
