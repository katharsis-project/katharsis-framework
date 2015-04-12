package io.katharsis.rs.controller.hk2.factory;

import io.katharsis.resource.ResourceInformationBuilder;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ResourceRegistryBuilder;
import io.katharsis.rs.controller.hk2.Hk2Context;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.ServiceLocator;

import javax.inject.Inject;

public class ResourceRegistryFactory implements Factory<ResourceRegistry> {

    private final ServiceLocator locator;

    @Inject
    public ResourceRegistryFactory(ServiceLocator locator) {
        this.locator = locator;
    }

    @Override
    public ResourceRegistry provide() {
        ResourceRegistryBuilder registryBuilder = new ResourceRegistryBuilder(new Hk2Context(locator), new ResourceInformationBuilder());
        return registryBuilder.build("io.katharsis.rs.resource", "https:/service.local");
    }

    @Override
    public void dispose(ResourceRegistry JsonPath) {

    }
}
