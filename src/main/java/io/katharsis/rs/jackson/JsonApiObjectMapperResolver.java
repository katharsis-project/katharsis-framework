package io.katharsis.rs.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.jackson.JsonApiModuleBuilder;
import io.katharsis.resource.registry.ResourceRegistry;
import org.glassfish.hk2.api.Factory;

import javax.inject.Inject;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

@Provider
public class JsonApiObjectMapperResolver implements ContextResolver<ObjectMapper>, Factory<ObjectMapper> {

    private ResourceRegistry resourceRegistry;
    private ObjectMapper defaultObjectMapper = new ObjectMapper();

    @Inject
    public JsonApiObjectMapperResolver(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        JsonApiModuleBuilder jsonApiModuleBuilder = new JsonApiModuleBuilder();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(jsonApiModuleBuilder.build(resourceRegistry));
        return objectMapper;
    }

    @Override
    public ObjectMapper provide() {
        return defaultObjectMapper;
    }

    @Override
    public void dispose(ObjectMapper objectMapper) {

    }
}
