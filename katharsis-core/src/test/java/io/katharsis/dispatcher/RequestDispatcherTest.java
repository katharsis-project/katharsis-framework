package io.katharsis.dispatcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.dispatcher.controller.collection.CollectionGet;
import io.katharsis.dispatcher.registry.ControllerRegistry;
import io.katharsis.errorhandling.ErrorResponse;
import io.katharsis.errorhandling.mapper.ExceptionMapperRegistryTest;
import io.katharsis.locator.SampleJsonServiceLocator;
import io.katharsis.module.ModuleRegistry;
import io.katharsis.queryspec.DefaultQuerySpecDeserializer;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.queryspec.internal.QuerySpecAdapterBuilder;
import io.katharsis.repository.RepositoryMethodParameterProvider;
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
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.HttpStatus;

public class RequestDispatcherTest {

    private ResourceRegistry resourceRegistry;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

	private ModuleRegistry moduleRegistry;

    @Before
    public void prepare() {
        ResourceInformationBuilder resourceInformationBuilder = new AnnotationResourceInformationBuilder(
            new ResourceFieldNameTransformer());
        ResourceRegistryBuilder registryBuilder = new ResourceRegistryBuilder(new SampleJsonServiceLocator(),
            resourceInformationBuilder);
        moduleRegistry = new ModuleRegistry();
        resourceRegistry = registryBuilder
            .build(ResourceRegistryBuilderTest.TEST_MODELS_PACKAGE, moduleRegistry, new ConstantServiceUrlProvider(ResourceRegistryTest.TEST_MODELS_URL));
        
        moduleRegistry.init(new ObjectMapper());
    }
    
    @Test
    public void onGivenPathAndRequestTypeControllerShouldHandleRequest() throws Exception {
        // GIVEN
        String path = "/tasks/";
        String requestType = "GET";

        PathBuilder pathBuilder = new PathBuilder(resourceRegistry);
        ControllerRegistry controllerRegistry = new ControllerRegistry(null);
        CollectionGet collectionGet = mock(CollectionGet.class);
        controllerRegistry.addController(collectionGet);
        QuerySpecAdapterBuilder queryAdapterBuilder = new QuerySpecAdapterBuilder(new DefaultQuerySpecDeserializer(), resourceRegistry);
        RequestDispatcher sut = new RequestDispatcher(moduleRegistry, controllerRegistry, null, queryAdapterBuilder);

        // WHEN
        when(collectionGet.isAcceptable(any(JsonPath.class), eq(requestType))).thenCallRealMethod();
        JsonPath jsonPath = pathBuilder.buildPath(path);
        Map<String, Set<String>> parameters = new HashMap<>();
		sut.dispatchRequest(jsonPath, requestType, parameters, null, null);

        // THEN
        verify(collectionGet, times(1)).handle(any(JsonPath.class), any(QueryAdapter.class), any(RepositoryMethodParameterProvider.class), any(RequestBody.class));
    }

    @Test
    public void shouldMapExceptionToErrorResponseIfMapperIsAvailable() throws Exception {

        ControllerRegistry controllerRegistry = mock(ControllerRegistry.class);
        //noinspection unchecked
        when(controllerRegistry.getController(any(JsonPath.class), anyString())).thenThrow(IllegalStateException.class);

        QuerySpecAdapterBuilder queryAdapterBuilder = new QuerySpecAdapterBuilder(new DefaultQuerySpecDeserializer(), resourceRegistry);
        RequestDispatcher requestDispatcher = new RequestDispatcher(moduleRegistry, controllerRegistry,
            ExceptionMapperRegistryTest.exceptionMapperRegistry, queryAdapterBuilder);

        BaseResponseContext response = requestDispatcher.dispatchRequest(null, null, null, null, null);
        assertThat(response)
            .isNotNull()
            .isExactlyInstanceOf(ErrorResponse.class);

        ErrorResponse errorResponse = (ErrorResponse) response;
        assertThat(errorResponse.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST_400);

    }

    @Test
    public void shouldThrowExceptionAsIsIfMapperIsNotAvailable() throws Exception {
        ControllerRegistry controllerRegistry = mock(ControllerRegistry.class);
        //noinspection unchecked
        when(controllerRegistry.getController(any(JsonPath.class), anyString())).thenThrow(ArithmeticException.class);

        QuerySpecAdapterBuilder queryAdapterBuilder = new QuerySpecAdapterBuilder(new DefaultQuerySpecDeserializer(), resourceRegistry);
        RequestDispatcher requestDispatcher = new RequestDispatcher(moduleRegistry, controllerRegistry,
            ExceptionMapperRegistryTest.exceptionMapperRegistry, queryAdapterBuilder);

        expectedException.expect(ArithmeticException.class);

        BaseResponseContext response = requestDispatcher.dispatchRequest(null, null, null, null, null);
    }
}
