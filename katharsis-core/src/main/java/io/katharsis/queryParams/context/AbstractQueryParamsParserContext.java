package io.katharsis.queryParams.context;


import io.katharsis.core.internal.dispatcher.path.JsonPath;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.registry.ResourceRegistry;

public abstract class AbstractQueryParamsParserContext implements QueryParamsParserContext {

    private final ResourceInformation resourceInformation;

    protected AbstractQueryParamsParserContext(ResourceRegistry resourceRegistry, JsonPath path) {
        resourceInformation = resourceRegistry.getEntry(path.getResourceName()).getResourceInformation();
    }

    @Override
    public ResourceInformation getRequestedResourceInformation() { return resourceInformation; }
}
