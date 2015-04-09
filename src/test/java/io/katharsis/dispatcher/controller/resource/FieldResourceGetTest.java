package io.katharsis.dispatcher.controller.resource;

import io.katharsis.context.SampleJsonApplicationContext;
import io.katharsis.path.JsonPath;
import io.katharsis.path.PathBuilder;
import io.katharsis.path.ResourcePath;
import io.katharsis.resource.ResourceInformationBuilder;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ResourceRegistryBuilder;
import io.katharsis.resource.registry.ResourceRegistryBuilderTest;
import io.katharsis.resource.registry.ResourceRegistryTest;
import io.katharsis.response.BaseResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class FieldResourceGetTest {
    private static final String REQUEST_TYPE = "GET";

    private PathBuilder pathBuilder;
    private ResourceRegistry resourceRegistry;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void prepare() {
        ResourceRegistryBuilder registryBuilder = new ResourceRegistryBuilder(new SampleJsonApplicationContext(), new ResourceInformationBuilder());
        resourceRegistry = registryBuilder.build(ResourceRegistryBuilderTest.TEST_MODELS_PACKAGE, ResourceRegistryTest.TEST_MODELS_URL);
        pathBuilder = new PathBuilder(resourceRegistry);
    }

    @Test
    public void onValidRequestShouldAcceptIt() {
        // GIVEN
        JsonPath jsonPath = pathBuilder.buildPath("tasks/1/project");
        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
        FieldResourceGet sut = new FieldResourceGet(resourceRegistry);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        assertThat(result).isTrue();
    }

    @Test
    public void onNonRelationRequestShouldDenyIt() {
        // GIVEN
        JsonPath jsonPath = new ResourcePath("tasks");
        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
        FieldResourceGet sut = new FieldResourceGet(resourceRegistry);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        assertThat(result).isFalse();
    }

    @Test
    public void onGivenRequestFieldResourceGetShouldHandleIt() {
        // GIVEN

        JsonPath jsonPath = pathBuilder.buildPath("/tasks/1/project");
        FieldResourceGet sut = new FieldResourceGet(resourceRegistry);

        // WHEN
        BaseResponse<?> response = sut.handle(jsonPath);

        // THEN
        Assert.assertNotNull(response);
    }

    @Test
    public void onGivenRequestFieldResourcesGetShouldHandleIt() {
        // GIVEN

        JsonPath jsonPath = pathBuilder.buildPath("/users/1/assignedProjects");
        FieldResourceGet sut = new FieldResourceGet(resourceRegistry);

        // WHEN
        BaseResponse<?> response = sut.handle(jsonPath);

        // THEN
        Assert.assertNotNull(response);
    }
}
