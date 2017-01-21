package io.katharsis.core.internal.jackson;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import com.fasterxml.jackson.databind.module.SimpleModule;

import io.katharsis.core.internal.jackson.JsonApiModuleBuilder;
import io.katharsis.resource.registry.ResourceRegistry;

public class JsonApiModuleBuilderTest {

    @Test
    public void onProvidedSerializerShouldCreateValidObjectMapper() throws Exception {
        // GIVEN
        JsonApiModuleBuilder sut = new JsonApiModuleBuilder();
        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);

        // WHEN
        SimpleModule result = sut.build(resourceRegistry, false);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getModuleName()).isEqualTo(JsonApiModuleBuilder.JSON_API_MODULE_NAME);
    }
}
