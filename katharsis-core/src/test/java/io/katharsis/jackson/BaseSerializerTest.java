package io.katharsis.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.dispatcher.registry.annotated.ParametersFactory;
import io.katharsis.locator.NewInstanceRepositoryFactory;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.request.path.JsonApiPath;
import io.katharsis.resource.field.ResourceFieldNameTransformer;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ResourceRegistryBuilder;
import io.katharsis.resource.registry.ResourceRegistryBuilderTest;
import io.katharsis.resource.registry.ResourceRegistryTest;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.response.ResourceResponseContext;
import org.junit.Before;

public abstract class BaseSerializerTest {

    protected ResourceRegistry resourceRegistry;
    protected ResourceResponseContext testResponse;
    ParametersFactory parametersFactory = new ParametersFactory();
    ObjectMapper sut;

    @Before
    public void setUp() throws Exception {
        ResourceInformationBuilder resourceInformationBuilder = new ResourceInformationBuilder(
                new ResourceFieldNameTransformer());
        ResourceRegistryBuilder registryBuilder = new ResourceRegistryBuilder(
                new NewInstanceRepositoryFactory(parametersFactory), resourceInformationBuilder);

        resourceRegistry = registryBuilder
                .build(ResourceRegistryBuilderTest.TEST_MODELS_PACKAGE, ResourceRegistryTest.TEST_MODELS_URL);

        JsonApiModuleBuilder jsonApiModuleBuilder = new JsonApiModuleBuilder();

        sut = new ObjectMapper();
        sut.registerModule(jsonApiModuleBuilder.build(resourceRegistry));

        JsonApiPath jsonPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/tasks");
        testResponse = new ResourceResponseContext(buildResponse(null), jsonPath, new QueryParams());
    }

    protected JsonApiResponse buildResponse(Object resource) {
        return new JsonApiResponse()
                .setEntity(resource);
    }
}
