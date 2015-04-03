package io.katharsis.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.katharsis.resource.registry.ResourceRegistry;

public class JsonApiModuleBuilder {

    public static final String JSON_API_MODULE_NAME = "JsonApiModule";

    public SimpleModule build(ResourceRegistry resourceRegistry) {
        SimpleModule simpleModule = new SimpleModule(JSON_API_MODULE_NAME,
                new Version(1, 0, 0, null, null, null));

        simpleModule.addSerializer(new ContainerSerializer(resourceRegistry))
                .addSerializer(new DataLinksContainerSerializer(resourceRegistry))
                .addSerializer(new RelationshipContainerSerializer(resourceRegistry));

        return simpleModule;
    }
}
