package io.katharsis.dispatcher.filter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import io.katharsis.dispatcher.RequestDispatcher;
import io.katharsis.dispatcher.controller.collection.CollectionGet;
import io.katharsis.dispatcher.registry.ControllerRegistry;
import io.katharsis.locator.SampleJsonServiceLocator;
import io.katharsis.module.ModuleRegistry;
import io.katharsis.module.SimpleModule;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.queryspec.internal.QueryParamsAdapter;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.repository.mock.NewInstanceRepositoryMethodParameterProvider;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.PathBuilder;
import io.katharsis.resource.field.ResourceFieldNameTransformer;
import io.katharsis.resource.information.AnnotationResourceInformationBuilder;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.registry.ConstantServiceUrlProvider;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ResourceRegistryBuilder;
import io.katharsis.resource.registry.ResourceRegistryBuilderTest;
import io.katharsis.resource.registry.ResourceRegistryTest;

public class FilterTest {

	private ResourceRegistry resourceRegistry;
	private TestFilter filter;
	private RequestDispatcher dispatcher;
	private CollectionGet collectionGet;
	private PathBuilder pathBuilder;
	private ModuleRegistry moduleRegistry;

	String path = "/tasks/";
	String requestType = "GET";

	@Before
	public void prepare() {
		moduleRegistry = new ModuleRegistry();
		
		ResourceInformationBuilder resourceInformationBuilder = new AnnotationResourceInformationBuilder(
				new ResourceFieldNameTransformer());
		ResourceRegistryBuilder registryBuilder = new ResourceRegistryBuilder(new SampleJsonServiceLocator(),
				resourceInformationBuilder);
		resourceRegistry = registryBuilder.build(ResourceRegistryBuilderTest.TEST_MODELS_PACKAGE,
				new ConstantServiceUrlProvider(ResourceRegistryTest.TEST_MODELS_URL));
		pathBuilder = new PathBuilder(resourceRegistry);
		ControllerRegistry controllerRegistry = new ControllerRegistry(null);
		collectionGet = mock(CollectionGet.class);
		controllerRegistry.addController(collectionGet);
		dispatcher = new RequestDispatcher(moduleRegistry, controllerRegistry, null);
	}

	@Test
	public void test() throws Exception {
		
		// GIVEN
		filter = mock(TestFilter.class);
		
		SimpleModule filterModule = new SimpleModule("filter");
		filterModule.addFilter(filter);
		moduleRegistry.addModule(filterModule);

		// WHEN
		ArgumentCaptor<FilterRequestContext> captor = ArgumentCaptor.forClass(FilterRequestContext.class);
		when(collectionGet.isAcceptable(any(JsonPath.class), eq(requestType))).thenCallRealMethod();
		when(filter.filter(any(FilterRequestContext.class), any(FilterChain.class))).thenCallRealMethod();
		JsonPath jsonPath = pathBuilder.buildPath(path);
		QueryParams queryParams = new QueryParams();
		RepositoryMethodParameterProvider parameterProvider = new NewInstanceRepositoryMethodParameterProvider();
		RequestBody requestBody = new RequestBody();
		dispatcher.dispatchRequest(jsonPath, requestType, new QueryParamsAdapter(queryParams), parameterProvider, requestBody );

		// THEN
		verify(filter).filter(captor.capture(), any(FilterChain.class));
		verify(collectionGet, times(1)).handle(any(JsonPath.class), any(QueryAdapter.class),
				any(RepositoryMethodParameterProvider.class), any(RequestBody.class));
		verify(filter, times(1)).filter(any(FilterRequestContext.class), any(FilterChain.class));

		FilterRequestContext value = captor.getValue();
		Assert.assertEquals("tasks", value.getJsonPath().getElementName());
		Assert.assertEquals(parameterProvider, value.getParameterProvider());
		Assert.assertEquals(queryParams, value.getQueryParams());
		Assert.assertEquals(requestBody, value.getRequestBody());
	}
}
