package io.katharsis.dispatcher.controller.resource;

import io.katharsis.dispatcher.controller.BaseController;
import io.katharsis.path.JsonPath;
import io.katharsis.path.LinksPath;
import io.katharsis.path.PathIds;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponse;

import java.lang.reflect.Field;
import java.util.Set;

public class LinkResourceGet implements BaseController {

    private ResourceRegistry resourceRegistry;

    public LinkResourceGet(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }

    @Override
    public boolean isAcceptable(JsonPath jsonPath, String requestType) {
        return !jsonPath.isCollection()
                && jsonPath instanceof LinksPath
                && "GET".equals(requestType);
    }

    @Override
    public BaseResponse<?> handle(JsonPath jsonPath) {
        String resourceName = jsonPath.getElementName();
        PathIds resourceIds = jsonPath.getIds();
        RegistryEntry registryEntry = resourceRegistry.getEntry(resourceName);
        Set<Field> relationshipFields = registryEntry.getResourceInformation().getRelationshipFields();
        Class<?> relationshipFieldClass = null;
        for (Field relationshipField : relationshipFields) {
            if (relationshipField.getName().equals(jsonPath.getElementName())) {
                relationshipFieldClass = relationshipField.getType();
            }
        }
        return null;
    }
}
