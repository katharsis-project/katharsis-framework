package io.katharsis.dispatcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.dispatcher.controller.collection.CollectionGet;
import io.katharsis.dispatcher.registry.ControllerRegistry;
import io.katharsis.locator.SampleJsonServiceLocator;
import io.katharsis.path.JsonPath;
import io.katharsis.path.PathBuilder;
import io.katharsis.queryParams.RequestParams;
import io.katharsis.resource.ResourceInformationBuilder;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ResourceRegistryBuilder;
import io.katharsis.resource.registry.ResourceRegistryBuilderTest;
import io.katharsis.resource.registry.ResourceRegistryTest;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class RequestDispatcherTest {

    private ResourceRegistry resourceRegistry;

    @Before
    public void prepare() {
        ResourceRegistryBuilder registryBuilder = new ResourceRegistryBuilder(new SampleJsonServiceLocator(), new ResourceInformationBuilder());
        resourceRegistry = registryBuilder.build(ResourceRegistryBuilderTest.TEST_MODELS_PACKAGE, ResourceRegistryTest.TEST_MODELS_URL);
    }

    @Test
    public void onGivenPathAndRequestTypeControllerShouldHandleRequest() throws Exception {
        // GIVEN
        String path = "/tasks/";
        String requestType = "GET";

        PathBuilder pathBuilder = new PathBuilder(resourceRegistry);
        ControllerRegistry controllerRegistry = new ControllerRegistry();
        CollectionGet collectionGet = mock(CollectionGet.class);
        controllerRegistry.addController(collectionGet);
        RequestDispatcher sut = new RequestDispatcher(controllerRegistry);

        // WHEN
        when(collectionGet.isAcceptable(any(JsonPath.class), eq(requestType))).thenCallRealMethod();
        JsonPath jsonPath = pathBuilder.buildPath(path);
        sut.dispatchRequest(jsonPath, requestType, new RequestParams(new ObjectMapper()), null);

        // THEN
        verify(collectionGet, times(1)).handle(any(JsonPath.class), any(RequestParams.class), null);
    }
}
