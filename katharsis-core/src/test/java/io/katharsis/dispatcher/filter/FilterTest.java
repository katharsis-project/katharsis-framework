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

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.dispatcher.RequestDispatcher;
import io.katharsis.dispatcher.controller.collection.CollectionGet;
import io.katharsis.dispatcher.registry.ControllerRegistry;
import io.katharsis.locator.SampleJsonServiceLocator;
import io.katharsis.module.ModuleRegistry;
import io.katharsis.module.SimpleModule;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.repository.mock.NewInstanceRepositoryMethodParameterProvider;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.PathBuilder;
import io.katharsis.resource.field.ResourceFieldNameTransformer;
import io.katharsis.resource.information.AnnotationResourceInformationBuilder;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ResourceRegistryBuilder;
import io.katharsis.resource.registry.ResourceRegistryBuilderTest;
import io.katharsis.resource.registry.ResourceRegistryTest;

public class FilterTest {

	private ResourceRegistry resourceRegistry;
	private ModuleRegistry moduleRegistry;
	private TestFilter filter;
	private SimpleModule testModule;
	private RequestDispatcher sut;
	private CollectionGet collectionGet;
	private PathBuilder pathBuilder;

	String path = "/tasks/";
	String requestType = "GET";

	@Before
	public void prepare() {
		ResourceInformationBuilder resourceInformationBuilder = new AnnotationResourceInformationBuilder(
				new ResourceFieldNameTransformer());
		ResourceRegistryBuilder registryBuilder = new ResourceRegistryBuilder(new SampleJsonServiceLocator(),
				resourceInformationBuilder);
		resourceRegistry = registryBuilder.build(ResourceRegistryBuilderTest.TEST_MODELS_PACKAGE,
				ResourceRegistryTest.TEST_MODELS_URL);
		moduleRegistry = new ModuleRegistry();

		testModule = new SimpleModule("test");
		pathBuilder = new PathBuilder(resourceRegistry);
		ControllerRegistry controllerRegistry = new ControllerRegistry(null);
		collectionGet = mock(CollectionGet.class);
		controllerRegistry.addController(collectionGet);
		sut = new RequestDispatcher(moduleRegistry, controllerRegistry, null);
	}

	@Test
	public void test() throws Exception {
		// GIVEN
		filter = mock(TestFilter.class);
		testModule.addFilter(filter);
		moduleRegistry.addModule(testModule);
		moduleRegistry.init(new ObjectMapper(), resourceRegistry);

		// WHEN
		ArgumentCaptor<FilterRequestContext> captor = ArgumentCaptor.forClass(FilterRequestContext.class);
		when(collectionGet.isAcceptable(any(JsonPath.class), eq(requestType))).thenCallRealMethod();
		when(filter.filter(any(FilterRequestContext.class), any(FilterChain.class))).thenCallRealMethod();
		JsonPath jsonPath = pathBuilder.buildPath(path);
		QueryParams queryParams = new QueryParams();
		RepositoryMethodParameterProvider parameterProvider = new NewInstanceRepositoryMethodParameterProvider();
		RequestBody requestBody = new RequestBody();
		sut.dispatchRequest(jsonPath, requestType, queryParams, parameterProvider, requestBody );

		// THEN
		verify(filter).filter(captor.capture(), any(FilterChain.class));
		verify(collectionGet, times(1)).handle(any(JsonPath.class), any(QueryParams.class),
				any(RepositoryMethodParameterProvider.class), any(RequestBody.class));
		verify(filter, times(1)).filter(any(FilterRequestContext.class), any(FilterChain.class));

		FilterRequestContext value = captor.getValue();
		Assert.assertEquals("tasks", value.getJsonPath().getElementName());
		Assert.assertEquals(parameterProvider, value.getParameterProvider());
		Assert.assertEquals(queryParams, value.getQueryParams());
		Assert.assertEquals(requestBody, value.getRequestBody());
	}
}
