package io.katharsis.dispatcher;

import io.katharsis.dispatcher.controller.CollectionGet;
import io.katharsis.dispatcher.registry.ControllerRegistry;
import io.katharsis.path.PathBuilder;
import io.katharsis.path.ResourcePath;
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
        RequestDispatcher sut = new RequestDispatcher(controllerRegistry, pathBuilder);

        // WHEN
        when(collectionGet.isAcceptable(any(ResourcePath.class), eq(requestType))).thenCallRealMethod();
        sut.dispatchRequest(path, requestType);

        // THEN
        verify(collectionGet, times(1)).handle(any(ResourcePath.class));
    }
}
