package io.katharsis.dispatcher.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.jackson.JsonApiModuleBuilder;
import io.katharsis.locator.SampleJsonServiceLocator;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.request.path.PathBuilder;
import io.katharsis.resource.field.ResourceFieldNameTransformer;
import io.katharsis.resource.include.IncludeLookupSetter;
import io.katharsis.resource.information.AnnotationResourceInformationBuilder;
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
    protected static final QueryParams REQUEST_PARAMS = new QueryParams();

    protected ObjectMapper objectMapper;
    protected PathBuilder pathBuilder;
    protected ResourceRegistry resourceRegistry;
    protected TypeParser typeParser;
    protected IncludeLookupSetter includeFieldSetter;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void prepare() {
        ResourceInformationBuilder resourceInformationBuilder = new AnnotationResourceInformationBuilder(
            new ResourceFieldNameTransformer());
        ResourceRegistryBuilder registryBuilder = new ResourceRegistryBuilder(new SampleJsonServiceLocator(),
            resourceInformationBuilder);
        resourceRegistry = registryBuilder
            .build(ResourceRegistryBuilderTest.TEST_MODELS_PACKAGE, ResourceRegistryTest.TEST_MODELS_URL);
        pathBuilder = new PathBuilder(resourceRegistry);
        typeParser = new TypeParser();
        includeFieldSetter = new IncludeLookupSetter(resourceRegistry);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JsonApiModuleBuilder().build(resourceRegistry));
    }
}
