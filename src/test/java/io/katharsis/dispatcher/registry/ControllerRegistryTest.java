package io.katharsis.dispatcher.registry;

import io.katharsis.path.PathBuilder;
import io.katharsis.path.ResourcePath;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ControllerRegistryTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void onUnsupportedRequestRegisterShouldThrowError() {
        // GIVEN
        PathBuilder pathBuilder = new PathBuilder();
        ResourcePath resourcePath = pathBuilder.buildPath("/resource/");
        String requestType = "PATCH";
        ControllerRegistry sut = new ControllerRegistry();

        // THEN
        expectedException.expect(IllegalStateException.class);

        // WHEN
        sut.getController(resourcePath, requestType);
    }
}
