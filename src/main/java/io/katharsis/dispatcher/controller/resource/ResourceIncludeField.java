package io.katharsis.dispatcher.controller.resource;

import io.katharsis.dispatcher.controller.BaseController;
import io.katharsis.resource.include.IncludeFieldSetter;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.utils.parser.TypeParser;

/**
 * Created by zachncst on 10/14/15.
 */
public abstract class ResourceIncludeField implements BaseController {
    protected final ResourceRegistry resourceRegistry;
    protected final TypeParser typeParser;
    protected final IncludeFieldSetter includeFieldSetter;

    public ResourceIncludeField(ResourceRegistry resourceRegistry, TypeParser typeParser, IncludeFieldSetter fieldSetter) {
        this.resourceRegistry = resourceRegistry;
        this.typeParser = typeParser;
        this.includeFieldSetter = fieldSetter;
    }
}
