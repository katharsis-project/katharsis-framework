package io.katharsis.dispatcher.registry;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.dispatcher.controller.BaseController;
import io.katharsis.dispatcher.controller.collection.CollectionGet;
import io.katharsis.dispatcher.controller.resource.FieldResourceGet;
import io.katharsis.dispatcher.controller.resource.FieldResourcePost;
import io.katharsis.dispatcher.controller.resource.RelationshipsResourceDelete;
import io.katharsis.dispatcher.controller.resource.RelationshipsResourceGet;
import io.katharsis.dispatcher.controller.resource.RelationshipsResourcePatch;
import io.katharsis.dispatcher.controller.resource.RelationshipsResourcePost;
import io.katharsis.dispatcher.controller.resource.ResourceDelete;
import io.katharsis.dispatcher.controller.resource.ResourceGet;
import io.katharsis.dispatcher.controller.resource.ResourcePatch;
import io.katharsis.dispatcher.controller.resource.ResourcePost;
import io.katharsis.resource.include.IncludeLookupSetter;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.utils.parser.TypeParser;

import java.util.HashSet;
import java.util.Set;

/**
 * This lookup gets all predefined Katharsis controllers.
 */
public class DefaultControllerLookup implements ControllerLookup {

    private ResourceRegistry resourceRegistry;
    private TypeParser typeParser;
    private ObjectMapper objectMapper;
    private IncludeLookupSetter includeFieldSetter;

    public DefaultControllerLookup(ResourceRegistry resourceRegistry, TypeParser typeParser, ObjectMapper objectMapper, IncludeLookupSetter includeFieldSetter) {
        this.resourceRegistry = resourceRegistry;
        this.typeParser = typeParser;
        this.objectMapper = objectMapper;
        this.includeFieldSetter = includeFieldSetter;
    }

    @Override
    public Set<BaseController> getControllers() {
        Set<BaseController> controllers = new HashSet<>();
        controllers.add(new RelationshipsResourceDelete(resourceRegistry, typeParser));
        controllers.add(new RelationshipsResourcePatch(resourceRegistry, typeParser));
        controllers.add(new RelationshipsResourcePost(resourceRegistry, typeParser));
        controllers.add(new ResourceDelete(resourceRegistry, typeParser));
        controllers.add(new CollectionGet(resourceRegistry, typeParser, includeFieldSetter));
        controllers.add(new FieldResourceGet(resourceRegistry, typeParser, includeFieldSetter));
        controllers.add(new RelationshipsResourceGet(resourceRegistry, typeParser, includeFieldSetter));
        controllers.add(new ResourceGet(resourceRegistry, typeParser, includeFieldSetter));
        controllers.add(new FieldResourcePost(resourceRegistry, typeParser, objectMapper));
        controllers.add(new ResourcePatch(resourceRegistry, typeParser, objectMapper));
        controllers.add(new ResourcePost(resourceRegistry, typeParser, objectMapper));

        return controllers;
    }

}
