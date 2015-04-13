package io.katharsis.dispatcher.registry;

import io.katharsis.dispatcher.controller.collection.CollectionGet;
import io.katharsis.dispatcher.controller.resource.FieldResourceGet;
import io.katharsis.dispatcher.controller.resource.LinksResourceGet;
import io.katharsis.dispatcher.controller.resource.ResourceGet;
import io.katharsis.dispatcher.controller.resource.ResourcePost;
import io.katharsis.path.PathBuilder;
import io.katharsis.resource.registry.ResourceRegistry;

/**
 * A builder class which holds all of the Katharsis controllers.
 */
public class ControllerRegistryBuilder {

    public ControllerRegistry build(ResourceRegistry resourceRegistry, PathBuilder pathBuilder) {
        CollectionGet collectionGet = new CollectionGet(resourceRegistry, pathBuilder);
        ResourceGet resourceGet = new ResourceGet(resourceRegistry, pathBuilder);
        LinksResourceGet linksResourceGet = new LinksResourceGet(resourceRegistry, pathBuilder);
        FieldResourceGet fieldResourceGet = new FieldResourceGet(resourceRegistry, pathBuilder);
        ResourcePost resourcePost = new ResourcePost(resourceRegistry, pathBuilder);
        return new ControllerRegistry(collectionGet, resourceGet, linksResourceGet, fieldResourceGet, resourcePost);
    }
}
