package io.katharsis.dispatcher.registry;

import java.util.HashSet;
import java.util.Set;

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
import io.katharsis.resource.internal.DocumentMapper;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.utils.parser.TypeParser;

/**
 * This lookup gets all predefined Katharsis controllers.
 */
public class DefaultControllerLookup implements ControllerLookup {

    private ResourceRegistry resourceRegistry;
    private TypeParser typeParser;
    private ObjectMapper objectMapper;
    private DocumentMapper documentMapper;

    public DefaultControllerLookup(ResourceRegistry resourceRegistry, TypeParser typeParser, ObjectMapper objectMapper, DocumentMapper documentMapper) {
        this.resourceRegistry = resourceRegistry;
        this.typeParser = typeParser;
        this.objectMapper = objectMapper;
        this.documentMapper = documentMapper;
    }

    @Override
    public Set<BaseController> getControllers() {
        Set<BaseController> controllers = new HashSet<>();
        controllers.add(new RelationshipsResourceDelete(resourceRegistry, typeParser));
        controllers.add(new RelationshipsResourcePatch(resourceRegistry, typeParser));
        controllers.add(new RelationshipsResourcePost(resourceRegistry, typeParser));
        controllers.add(new ResourceDelete(resourceRegistry, typeParser));
        controllers.add(new CollectionGet(resourceRegistry, objectMapper, typeParser, documentMapper));
        controllers.add(new FieldResourceGet(resourceRegistry, objectMapper, typeParser, documentMapper));
        controllers.add(new RelationshipsResourceGet(resourceRegistry, objectMapper, typeParser, documentMapper));
        controllers.add(new ResourceGet(resourceRegistry, objectMapper, typeParser, documentMapper));
        controllers.add(new FieldResourcePost(resourceRegistry, typeParser, objectMapper, documentMapper));
        controllers.add(new ResourcePatch(resourceRegistry, typeParser, objectMapper, documentMapper));
        controllers.add(new ResourcePost(resourceRegistry, typeParser, objectMapper, documentMapper));

        return controllers;
    }

}
