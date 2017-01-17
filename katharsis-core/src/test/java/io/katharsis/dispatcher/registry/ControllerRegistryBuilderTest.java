package io.katharsis.dispatcher.registry;

import org.junit.Test;

import io.katharsis.request.path.ResourcePath;

public class ControllerRegistryBuilderTest {

    @Test
    public void onBuildShouldAddAllControllers() throws Exception {
        // GIVEN
        ControllerRegistryBuilder sut = new ControllerRegistryBuilder(null, null, null, null);

        // WHEN
        ControllerRegistry result = sut.build();

        // THEN
        result.getController(new ResourcePath("path"), "GET");
    }
}
