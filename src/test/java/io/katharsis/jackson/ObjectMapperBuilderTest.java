package io.katharsis.jackson;

import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.resource.registry.ResourceRegistry;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ObjectMapperBuilderTest {

    @Test
    public void onProvidedSerializerShouldCreateValidObjectMapper() throws Exception {
        // GIVEN
        ObjectMapperBuilder sut = new ObjectMapperBuilder();
        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
        JsonSerializer jsonSerializer = spy(new ContainerSerializer(resourceRegistry));

        // WHEN
        ObjectMapper result = sut.buildWith(jsonSerializer);

        // THEN
        assertThat(result).isNotNull();
        verify(jsonSerializer, times(1)).handledType();
    }
}
