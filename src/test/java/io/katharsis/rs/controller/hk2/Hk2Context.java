package io.katharsis.rs.controller.hk2;

import io.katharsis.context.JsonApplicationContext;
import org.glassfish.hk2.api.ServiceLocator;

public class Hk2Context implements JsonApplicationContext {

    private final ServiceLocator locator;

    public Hk2Context(ServiceLocator locator) {
        this.locator = locator;
    }

    @Override
    public <T> T getInstance(Class<T> clazz) {
        return locator.getService(clazz);
    }
}
