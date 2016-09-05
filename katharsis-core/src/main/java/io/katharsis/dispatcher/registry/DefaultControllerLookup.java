package io.katharsis.dispatcher.registry;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.dispatcher.controller.BaseController;
import io.katharsis.dispatcher.controller.collection.CollectionGet;
import io.katharsis.dispatcher.controller.collection.CollectionHead;
import io.katharsis.dispatcher.controller.field.FieldResourceGet;
import io.katharsis.dispatcher.controller.field.FieldResourceHead;
import io.katharsis.dispatcher.controller.field.FieldResourcePost;
import io.katharsis.dispatcher.controller.relationship.*;
import io.katharsis.dispatcher.controller.resource.*;
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
        controllers.add(new CollectionGet(resourceRegistry, typeParser, includeFieldSetter));
        controllers.add(new CollectionHead(resourceRegistry, typeParser, includeFieldSetter));

        controllers.add(new ResourceGet(resourceRegistry, typeParser, includeFieldSetter));
        controllers.add(new ResourceHead(resourceRegistry, typeParser, includeFieldSetter));
        controllers.add(new ResourcePost(resourceRegistry, typeParser, objectMapper));
        controllers.add(new ResourcePatch(resourceRegistry, typeParser, objectMapper));
        controllers.add(new ResourceDelete(resourceRegistry, typeParser));

        controllers.add(new RelationshipsResourceGet(resourceRegistry, typeParser, includeFieldSetter));
        controllers.add(new RelationshipsResourceHead(resourceRegistry, typeParser, includeFieldSetter));
        controllers.add(new RelationshipsResourcePost(resourceRegistry, typeParser));
        controllers.add(new RelationshipsResourcePatch(resourceRegistry, typeParser));
        controllers.add(new RelationshipsResourceDelete(resourceRegistry, typeParser));

        controllers.add(new FieldResourceGet(resourceRegistry, typeParser, includeFieldSetter));
        controllers.add(new FieldResourceHead(resourceRegistry, typeParser, includeFieldSetter));
        controllers.add(new FieldResourcePost(resourceRegistry, typeParser, objectMapper));

        return controllers;
    }

}
