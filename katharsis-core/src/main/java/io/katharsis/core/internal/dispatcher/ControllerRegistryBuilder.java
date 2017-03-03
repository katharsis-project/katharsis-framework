package io.katharsis.core.internal.dispatcher;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.core.internal.boot.PropertiesProvider;
import io.katharsis.core.internal.dispatcher.controller.BaseController;
import io.katharsis.core.internal.exception.DefaultExceptionMapperLookup;
import io.katharsis.core.internal.resource.DocumentMapper;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.utils.parser.TypeParser;

/**
 * A builder class which holds all of the Katharsis controllers, which must be placed in
 * {@link io.katharsis.core.internal.dispatcher.controller} package.
 */
public class ControllerRegistryBuilder {

    private final ResourceRegistry resourceRegistry;
    private final TypeParser typeParser;
    private final ObjectMapper objectMapper;
    private final DocumentMapper documentMapper;

    public ControllerRegistryBuilder(@SuppressWarnings("SameParameterValue") ResourceRegistry resourceRegistry, @SuppressWarnings("SameParameterValue") TypeParser typeParser,
                                     @SuppressWarnings("SameParameterValue") ObjectMapper objectMapper, PropertiesProvider propertiesProvider) {
        this.resourceRegistry = resourceRegistry;
        this.typeParser = typeParser;
        this.objectMapper = objectMapper;
        this.documentMapper = new DocumentMapper(resourceRegistry, objectMapper, propertiesProvider);
    }

    /**
     * Uses the {@link DefaultExceptionMapperLookup} to collect all controllers.
     *
     * @return an instance of {@link ControllerRegistry} with initialized controllers
     */
    public ControllerRegistry build() {
        return build(new DefaultControllerLookup(resourceRegistry, typeParser, objectMapper, documentMapper));
    }

    /**
     * Uses the given {@link ControllerLookup} to get all controllers.
     *
     * @param lookup an instance of a lookup class to get the controllers
     * @return an instance of {@link ControllerRegistry} with initialized controllers
     */
    private static ControllerRegistry build(ControllerLookup lookup) {
        List<BaseController> controllers = new LinkedList<>();
        controllers.addAll(lookup.getControllers());
        return new ControllerRegistry(controllers);
    }

	public DocumentMapper getDocumentMapper() {
		return documentMapper;
	}
}
