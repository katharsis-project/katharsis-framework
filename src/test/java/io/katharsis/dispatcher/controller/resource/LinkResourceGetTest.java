package io.katharsis.dispatcher.controller.resource;

import io.katharsis.path.JsonPath;
import io.katharsis.path.PathBuilder;
import io.katharsis.resource.registry.ResourceRegistry;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class LinkResourceGetTest {

    private static final String REQUEST_TYPE = "GET";

    private PathBuilder pathBuilder;

    @Before
    public void prepare() {
        pathBuilder = new PathBuilder();
    }

    @Test
    public void onValidRequestShouldAcceptIt() {
        // GIVEN
        JsonPath jsonPath = pathBuilder.buildPath("test/1/links/asd");
        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
        LinkResourceGet sut = new LinkResourceGet(resourceRegistry);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        assertThat(result).isTrue();
    }

    @Test
    public void onNonRelationRequestShouldDenyIt() {
        // GIVEN
        JsonPath jsonPath = new JsonPath("test");
        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
        LinkResourceGet sut = new LinkResourceGet(resourceRegistry);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        assertThat(result).isFalse();
    }
}
