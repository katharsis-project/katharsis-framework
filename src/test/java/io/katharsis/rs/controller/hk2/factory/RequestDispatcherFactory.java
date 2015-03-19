package io.katharsis.rs.controller.hk2.factory;

import io.katharsis.dispatcher.RequestDispatcher;
import io.katharsis.dispatcher.registry.ControllerRegistry;
import io.katharsis.dispatcher.registry.ControllerRegistryBuilder;
import io.katharsis.resource.registry.ResourceRegistry;
import org.glassfish.hk2.api.Factory;

import javax.inject.Inject;

public class RequestDispatcherFactory implements Factory<RequestDispatcher> {

    private ResourceRegistry resourceRegistry;

    @Inject
    public RequestDispatcherFactory(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }

    @Override
    public RequestDispatcher provide() {
        ControllerRegistryBuilder controllerRegistryBuilder = new ControllerRegistryBuilder();
        ControllerRegistry controllerRegistry = controllerRegistryBuilder.build(resourceRegistry);
        return new RequestDispatcher(controllerRegistry);
    }

    @Override
    public void dispose(RequestDispatcher requestDispatcher) {

    }
}
