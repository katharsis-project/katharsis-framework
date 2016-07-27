package io.katharsis.dispatcher.registry;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.dispatcher.controller.BaseController;
import io.katharsis.errorhandling.mapper.DefaultExceptionMapperLookup;
import io.katharsis.resource.include.IncludeLookupSetter;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.utils.parser.TypeParser;

import java.util.LinkedList;
import java.util.List;

/**
 * A builder class which holds all of the Katharsis controllers, which must be placed in
 * {@link io.katharsis.dispatcher.controller} package.
 */
public class ControllerRegistryBuilder {

    private final ResourceRegistry resourceRegistry;
    private final TypeParser typeParser;
    private final ObjectMapper objectMapper;
    private final IncludeLookupSetter includeFieldSetter;

    public ControllerRegistryBuilder(@SuppressWarnings("SameParameterValue") ResourceRegistry resourceRegistry, @SuppressWarnings("SameParameterValue") TypeParser typeParser,
                                     @SuppressWarnings("SameParameterValue") ObjectMapper objectMapper) {
        this.resourceRegistry = resourceRegistry;
        this.typeParser = typeParser;
        this.objectMapper = objectMapper;
        this.includeFieldSetter = new IncludeLookupSetter(resourceRegistry);
    }

    /**
     * Uses the {@link DefaultExceptionMapperLookup} to collect all controllers.
     *
     * @return an instance of {@link ControllerRegistry} with initialized controllers
     */
    public ControllerRegistry build() {
        return build(new DefaultControllerLookup(resourceRegistry, typeParser, objectMapper, includeFieldSetter));
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
}
