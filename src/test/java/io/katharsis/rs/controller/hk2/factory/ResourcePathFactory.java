package io.katharsis.rs.controller.hk2.factory;

import io.katharsis.path.PathBuilder;
import io.katharsis.path.ResourcePath;
import org.glassfish.hk2.api.Factory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;

public class ResourcePathFactory implements Factory<ResourcePath> {

    private final HttpServletRequest request;

    @Inject
    public ResourcePathFactory(Provider<HttpServletRequest> requestProvider) {
        this.request = requestProvider.get();
    }

    @Override
    public ResourcePath provide() {
        return new PathBuilder().buildPath(request.getRequestURI());
    }

    @Override
    public void dispose(ResourcePath resourcePath) {

    }
}
