package io.katharsis.rs.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.jackson.ContainerSerializer;
import io.katharsis.jackson.DataLinksContainerSerializer;
import io.katharsis.jackson.ObjectMapperBuilder;
import io.katharsis.jackson.RelationshipContainerSerializer;
import io.katharsis.resource.registry.ResourceRegistry;

import javax.inject.Inject;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

@Provider
public class JsonApiObjectMapperResolver implements ContextResolver<ObjectMapper> {

    private ResourceRegistry resourceRegistry;

    @Inject
    public JsonApiObjectMapperResolver(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        ObjectMapperBuilder objectMapperBuilder = new ObjectMapperBuilder();
        ObjectMapper objectMapper = objectMapperBuilder.buildWith(new ContainerSerializer(resourceRegistry),
                new DataLinksContainerSerializer(resourceRegistry),
                new RelationshipContainerSerializer(resourceRegistry));
        return objectMapper;
    }
}
