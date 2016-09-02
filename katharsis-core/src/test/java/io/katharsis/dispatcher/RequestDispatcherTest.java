package io.katharsis.dispatcher;

import io.katharsis.dispatcher.controller.collection.CollectionGet;
import io.katharsis.dispatcher.registry.ControllerRegistry;
import io.katharsis.errorhandling.ErrorResponse;
import io.katharsis.errorhandling.mapper.ExceptionMapperRegistryTest;
import io.katharsis.locator.SampleJsonServiceLocator;
import io.katharsis.module.ModuleRegistry;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.PathBuilder;
import io.katharsis.resource.field.ResourceFieldNameTransformer;
import io.katharsis.resource.information.AnnotationResourceInformationBuilder;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.registry.*;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.HttpStatus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

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
        resourceRegistry = registryBuilder
            .build(ResourceRegistryBuilderTest.TEST_MODELS_PACKAGE, new ConstantServiceUrlProvider(ResourceRegistryTest.TEST_MODELS_URL));
        
        moduleRegistry = new ModuleRegistry();
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
        RequestDispatcher sut = new RequestDispatcher(moduleRegistry, controllerRegistry, null);

        // WHEN
        when(collectionGet.isAcceptable(any(JsonPath.class), eq(requestType))).thenCallRealMethod();
        JsonPath jsonPath = pathBuilder.buildPath(path);
        sut.dispatchRequest(jsonPath, requestType, new QueryParams(), null, null);

        // THEN
        verify(collectionGet, times(1)).handle(any(JsonPath.class), any(QueryParams.class), any(RepositoryMethodParameterProvider.class), any(RequestBody.class));
    }

    @Test
    public void shouldMapExceptionToErrorResponseIfMapperIsAvailable() throws Exception {

        ControllerRegistry controllerRegistry = mock(ControllerRegistry.class);
        //noinspection unchecked
        when(controllerRegistry.getController(any(JsonPath.class), anyString())).thenThrow(IllegalStateException.class);

        RequestDispatcher requestDispatcher = new RequestDispatcher(moduleRegistry, controllerRegistry,
            ExceptionMapperRegistryTest.exceptionMapperRegistry);

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

        RequestDispatcher requestDispatcher = new RequestDispatcher(moduleRegistry, controllerRegistry,
            ExceptionMapperRegistryTest.exceptionMapperRegistry);

        expectedException.expect(ArithmeticException.class);

        BaseResponseContext response = requestDispatcher.dispatchRequest(null, null, null, null, null);
    }
}
