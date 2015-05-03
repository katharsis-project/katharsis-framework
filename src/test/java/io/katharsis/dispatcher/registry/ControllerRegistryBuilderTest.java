package io.katharsis.dispatcher.registry;

import io.katharsis.request.path.ResourcePath;
import org.junit.Test;

public class ControllerRegistryBuilderTest {

    @Test
    public void onBuildShouldAddAllControllers() throws Exception {
        // GIVEN
        ControllerRegistryBuilder sut = new ControllerRegistryBuilder();

        // WHEN
        ControllerRegistry result = sut.build(null);

        // THEN
        result.getController(new ResourcePath("path"), "GET");
    }
}
