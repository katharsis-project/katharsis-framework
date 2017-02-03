package io.katharsis.core.internal.dispatcher.registry;

import org.junit.Test;

import io.katharsis.core.internal.dispatcher.ControllerRegistry;
import io.katharsis.core.internal.dispatcher.ControllerRegistryBuilder;
import io.katharsis.core.internal.dispatcher.path.ResourcePath;

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
