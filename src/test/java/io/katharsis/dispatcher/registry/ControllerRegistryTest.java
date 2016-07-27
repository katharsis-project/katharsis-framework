package io.katharsis.dispatcher.registry;

import io.katharsis.locator.SampleJsonServiceLocator;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.PathBuilder;
import io.katharsis.resource.field.ResourceFieldNameTransformer;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ResourceRegistryBuilder;
import io.katharsis.resource.registry.ResourceRegistryBuilderTest;
import io.katharsis.resource.registry.ResourceRegistryTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ControllerRegistryTest {

    private ResourceRegistry resourceRegistry;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void prepare() {
        ResourceInformationBuilder resourceInformationBuilder = new ResourceInformationBuilder(
            new ResourceFieldNameTransformer());
        ResourceRegistryBuilder registryBuilder = new ResourceRegistryBuilder(new SampleJsonServiceLocator(),
            resourceInformationBuilder);
        resourceRegistry = registryBuilder
            .build(ResourceRegistryBuilderTest.TEST_MODELS_PACKAGE, ResourceRegistryTest.TEST_MODELS_URL);
    }

    @Test
    public void onUnsupportedRequestRegisterShouldThrowError() {
        // GIVEN
        PathBuilder pathBuilder = new PathBuilder(resourceRegistry);
        JsonPath jsonPath = pathBuilder.buildPath("/tasks/");
        String requestType = "PATCH";
        ControllerRegistry sut = new ControllerRegistry(null);

        // THEN
        expectedException.expect(MethodNotFoundException.class);

        // WHEN
        sut.getController(jsonPath, requestType);
    }
}
