package io.katharsis.dispatcher.registry;

import io.katharsis.dispatcher.controller.collection.CollectionGet;
import io.katharsis.dispatcher.controller.resource.LinkResourceGet;
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
        LinkResourceGet linkResourceGet = new LinkResourceGet(resourceRegistry);
        ResourcePost resourcePost = new ResourcePost(resourceRegistry);
        return new ControllerRegistry(collectionGet, resourceGet, linkResourceGet, resourcePost);
    }
}
