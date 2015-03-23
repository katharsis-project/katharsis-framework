package io.katharsis.rs.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.katharsis.jackson.ContainerSerializer;
import io.katharsis.jackson.LinksContainerSerializer;
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
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule simpleModule = new SimpleModule("SimpleModule",
                new Version(1, 0, 0, null, null, null));
        simpleModule.addSerializer(new ContainerSerializer(resourceRegistry));
        simpleModule.addSerializer(new LinksContainerSerializer(resourceRegistry));
        objectMapper.registerModule(simpleModule);
        return objectMapper;
    }
}
