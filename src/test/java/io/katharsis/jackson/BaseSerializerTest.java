package io.katharsis.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.locator.SampleJsonServiceLocator;
import io.katharsis.resource.field.ResourceFieldNameTransformer;
import io.katharsis.resource.infromation.ResourceInformationBuilder;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ResourceRegistryBuilder;
import io.katharsis.resource.registry.ResourceRegistryBuilderTest;
import io.katharsis.resource.registry.ResourceRegistryTest;
import org.junit.Before;

public abstract class BaseSerializerTest {

    ObjectMapper sut;

    @Before
    public void setUp() throws Exception {
        ResourceInformationBuilder resourceInformationBuilder = new ResourceInformationBuilder(
            new ResourceFieldNameTransformer());
        ResourceRegistryBuilder registryBuilder = new ResourceRegistryBuilder(new SampleJsonServiceLocator(),
            resourceInformationBuilder);
        ResourceRegistry resourceRegistry = registryBuilder
            .build(ResourceRegistryBuilderTest.TEST_MODELS_PACKAGE, ResourceRegistryTest.TEST_MODELS_URL);

        JsonApiModuleBuilder jsonApiModuleBuilder = new JsonApiModuleBuilder();

        sut = new ObjectMapper();
        sut.registerModule(jsonApiModuleBuilder.build(resourceRegistry));
    }
}
