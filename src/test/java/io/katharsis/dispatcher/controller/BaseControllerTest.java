package io.katharsis.dispatcher.controller;

import io.katharsis.locator.SampleJsonServiceLocator;
import io.katharsis.request.path.PathBuilder;
import io.katharsis.resource.ResourceInformationBuilder;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ResourceRegistryBuilder;
import io.katharsis.resource.registry.ResourceRegistryBuilderTest;
import io.katharsis.resource.registry.ResourceRegistryTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

public abstract class BaseControllerTest {

    protected PathBuilder pathBuilder;
    protected ResourceRegistry resourceRegistry;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void prepare() {
        ResourceRegistryBuilder registryBuilder = new ResourceRegistryBuilder(new SampleJsonServiceLocator(), new ResourceInformationBuilder());
        resourceRegistry = registryBuilder.build(ResourceRegistryBuilderTest.TEST_MODELS_PACKAGE, ResourceRegistryTest.TEST_MODELS_URL);
        pathBuilder = new PathBuilder(resourceRegistry);
    }
}
