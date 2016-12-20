package io.katharsis.dispatcher.controller.resource;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.dispatcher.controller.BaseController;
import io.katharsis.resource.include.IncludeLookupSetter;
import io.katharsis.resource.internal.DocumentMapper;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.utils.parser.TypeParser;

/**
 * Created by zachncst on 10/14/15.
 */
public abstract class ResourceIncludeField extends BaseController {
    protected final ResourceRegistry resourceRegistry;
    protected final TypeParser typeParser;
    protected final IncludeLookupSetter includeFieldSetter;
    
	protected DocumentMapper documentMapper;

    public ResourceIncludeField(ResourceRegistry resourceRegistry, ObjectMapper objectMapper, TypeParser typeParser, IncludeLookupSetter fieldSetter) {
        this.resourceRegistry = resourceRegistry;
        this.typeParser = typeParser;
        this.includeFieldSetter = fieldSetter;
        this.documentMapper = new DocumentMapper(resourceRegistry, objectMapper);
    }
}
