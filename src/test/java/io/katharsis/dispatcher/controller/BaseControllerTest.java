package io.katharsis.dispatcher.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.locator.SampleJsonServiceLocator;
import io.katharsis.request.path.PathBuilder;
import io.katharsis.resource.field.ResourceFieldNameTransformer;
import io.katharsis.resource.include.IncludeFieldSetter;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ResourceRegistryBuilder;
import io.katharsis.resource.registry.ResourceRegistryBuilderTest;
import io.katharsis.resource.registry.ResourceRegistryTest;
import io.katharsis.utils.parser.TypeParser;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

public abstract class BaseControllerTest {

    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    protected PathBuilder pathBuilder;
    protected ResourceRegistry resourceRegistry;
    protected TypeParser typeParser;
    protected IncludeFieldSetter includeFieldSetter;

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
        pathBuilder = new PathBuilder(resourceRegistry);
        typeParser = new TypeParser();
        includeFieldSetter = new IncludeFieldSetter(resourceRegistry);
    }
}
