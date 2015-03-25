package io.katharsis.dispatcher;

import io.katharsis.dispatcher.controller.collection.CollectionGet;
import io.katharsis.dispatcher.registry.ControllerRegistry;
import io.katharsis.path.JsonPath;
import io.katharsis.path.PathBuilder;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class RequestDispatcherTest {

    @Test
    public void onGivenPathAndRequestTypeControllerShouldHandleRequest() throws Exception {
        // GIVEN
        String path = "/resource/";
        String requestType = "GET";

        PathBuilder pathBuilder = new PathBuilder();
        ControllerRegistry controllerRegistry = new ControllerRegistry();
        CollectionGet collectionGet = mock(CollectionGet.class);
        controllerRegistry.addController(collectionGet);
        RequestDispatcher sut = new RequestDispatcher(controllerRegistry);

        // WHEN
        when(collectionGet.isAcceptable(any(JsonPath.class), eq(requestType))).thenCallRealMethod();
        JsonPath jsonPath = pathBuilder.buildPath(path);
        sut.dispatchRequest(jsonPath, requestType);

        // THEN
        verify(collectionGet, times(1)).handle(any(JsonPath.class));
    }
}
