package io.katharsis.dispatcher.registry;

import io.katharsis.dispatcher.controller.collection.CollectionGet;
import io.katharsis.dispatcher.controller.resource.FieldResourceGet;
import io.katharsis.dispatcher.controller.resource.LinksResourceGet;
import io.katharsis.dispatcher.controller.resource.ResourceGet;
import io.katharsis.dispatcher.controller.resource.ResourcePost;
import io.katharsis.resource.registry.ResourceRegistry;

/**
 * A builder class which holds all of the Katharsis controllers.
 */
public class ControllerRegistryBuilder {

    public ControllerRegistry build(ResourceRegistry resourceRegistry) {
        CollectionGet collectionGet = new CollectionGet(resourceRegistry);
        ResourceGet resourceGet = new ResourceGet(resourceRegistry);
        LinksResourceGet linksResourceGet = new LinksResourceGet(resourceRegistry);
        FieldResourceGet fieldResourceGet = new FieldResourceGet(resourceRegistry);
        ResourcePost resourcePost = new ResourcePost(resourceRegistry);
        return new ControllerRegistry(collectionGet, resourceGet, linksResourceGet, fieldResourceGet, resourcePost);
    }
}
