package io.katharsis.dispatcher.controller.resource;

import io.katharsis.dispatcher.controller.BaseController;
import io.katharsis.path.JsonPath;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponse;

public class LinkResourceGet implements BaseController {

    private ResourceRegistry resourceRegistry;

    public LinkResourceGet(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }

    @Override
    public boolean isAcceptable(JsonPath jsonPath, String requestType) {
        return !jsonPath.isCollection()
                && jsonPath.isRelationship()
                && "GET".equals(requestType);
    }

    @Override
    public BaseResponse<?> handle(JsonPath jsonPath) {
        return null;
    }
}
