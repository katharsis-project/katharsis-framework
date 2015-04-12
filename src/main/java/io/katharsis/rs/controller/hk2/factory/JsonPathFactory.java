package io.katharsis.rs.controller.hk2.factory;

import io.katharsis.path.JsonPath;
import io.katharsis.path.PathBuilder;
import io.katharsis.resource.registry.ResourceRegistry;
import org.glassfish.hk2.api.Factory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;

public class JsonPathFactory implements Factory<JsonPath> {

    private final HttpServletRequest request;
    private ResourceRegistry resourceRegistry;

    @Inject
    public JsonPathFactory(Provider<HttpServletRequest> requestProvider, ResourceRegistry resourceRegistry) {
        this.request = requestProvider.get();
        this.resourceRegistry = resourceRegistry;
    }

    @Override
    public JsonPath provide() {
        return new PathBuilder(resourceRegistry).buildPath(request.getRequestURI());
    }

    @Override
    public void dispose(JsonPath JsonPath) {

    }
}
