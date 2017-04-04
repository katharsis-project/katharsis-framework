package io.katharsis.core.internal.dispatcher.filter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.core.internal.dispatcher.ControllerRegistry;
import io.katharsis.core.internal.dispatcher.RequestDispatcher;
import io.katharsis.core.internal.dispatcher.controller.CollectionGet;
import io.katharsis.core.internal.dispatcher.path.JsonPath;
import io.katharsis.core.internal.dispatcher.path.PathBuilder;
import io.katharsis.core.internal.query.QuerySpecAdapterBuilder;
import io.katharsis.core.internal.resource.AnnotationResourceInformationBuilder;
import io.katharsis.legacy.internal.RepositoryMethodParameterProvider;
import io.katharsis.legacy.locator.SampleJsonServiceLocator;
import io.katharsis.legacy.registry.ResourceRegistryBuilder;
import io.katharsis.module.ModuleRegistry;
import io.katharsis.module.SimpleModule;
import io.katharsis.queryspec.DefaultQuerySpecDeserializer;
import io.katharsis.repository.filter.DocumentFilterChain;
import io.katharsis.repository.filter.DocumentFilterContext;
import io.katharsis.repository.mock.NewInstanceRepositoryMethodParameterProvider;
import io.katharsis.repository.request.QueryAdapter;
import io.katharsis.resource.Document;
import io.katharsis.resource.information.ResourceFieldNameTransformer;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.registry.ConstantServiceUrlProvider;
import io.katharsis.resource.registry.ResourceRegistry;
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

		ResourceInformationBuilder resourceInformationBuilder = new AnnotationResourceInformationBuilder(new ResourceFieldNameTransformer());
		ResourceRegistryBuilder registryBuilder = new ResourceRegistryBuilder(moduleRegistry, new SampleJsonServiceLocator(), resourceInformationBuilder);
		resourceRegistry = registryBuilder.build(ResourceRegistryBuilderTest.TEST_MODELS_PACKAGE, moduleRegistry, new ConstantServiceUrlProvider(ResourceRegistryTest.TEST_MODELS_URL));

		pathBuilder = new PathBuilder(resourceRegistry);
		ControllerRegistry controllerRegistry = new ControllerRegistry(null);
		collectionGet = mock(CollectionGet.class);
		controllerRegistry.addController(collectionGet);
		QuerySpecAdapterBuilder queryAdapterBuilder = new QuerySpecAdapterBuilder(new DefaultQuerySpecDeserializer(), moduleRegistry);
		dispatcher = new RequestDispatcher(moduleRegistry, controllerRegistry, null, queryAdapterBuilder);
	}

	@Test
	public void test() throws Exception {

		// GIVEN
		filter = mock(TestFilter.class);

		SimpleModule filterModule = new SimpleModule("filter");
		filterModule.addFilter(filter);
		moduleRegistry.addModule(filterModule);
		moduleRegistry.init(new ObjectMapper());

		// WHEN
		ArgumentCaptor<DocumentFilterContext> captor = ArgumentCaptor.forClass(DocumentFilterContext.class);
		when(collectionGet.isAcceptable(any(JsonPath.class), eq(requestType))).thenCallRealMethod();
		when(filter.filter(any(DocumentFilterContext.class), any(DocumentFilterChain.class))).thenCallRealMethod();
		JsonPath jsonPath = pathBuilder.buildPath(path);
		Map<String, Set<String>> queryParams = new HashMap<>();
		RepositoryMethodParameterProvider parameterProvider = new NewInstanceRepositoryMethodParameterProvider();
		Document requestBody = new Document();
		dispatcher.dispatchRequest(jsonPath, requestType, queryParams, parameterProvider, requestBody);

		// THEN
		verify(filter).filter(captor.capture(), any(DocumentFilterChain.class));
		verify(collectionGet, times(1)).handle(any(JsonPath.class), any(QueryAdapter.class), any(RepositoryMethodParameterProvider.class), any(Document.class));
		verify(filter, times(1)).filter(any(DocumentFilterContext.class), any(DocumentFilterChain.class));

		DocumentFilterContext value = captor.getValue();
		Assert.assertEquals("tasks", value.getJsonPath().getElementName());
		Assert.assertEquals(parameterProvider, value.getParameterProvider());
		Assert.assertEquals(requestBody, value.getRequestBody());
		Assert.assertEquals("GET", value.getMethod());
	}
}
