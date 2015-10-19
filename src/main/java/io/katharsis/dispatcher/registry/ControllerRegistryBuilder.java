package io.katharsis.dispatcher.registry;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.dispatcher.controller.BaseController;
import io.katharsis.dispatcher.controller.resource.ResourceIncludeField;
import io.katharsis.dispatcher.controller.resource.ResourceUpsert;
import io.katharsis.resource.include.IncludeLookupSetter;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.utils.parser.TypeParser;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
     * Scans an internal Katharsis package for controllers and then instantiates them.
     *
     * @return an instance of {@link ControllerRegistry} with initialized controllers
     * @throws Exception initialization exception
     */
    public ControllerRegistry build() throws Exception {


        Reflections reflections = new Reflections("io.katharsis.dispatcher.controller");

        Set<Class<? extends BaseController>> controllerClasses =
                reflections.getSubTypesOf(BaseController.class);

        List<BaseController> controllers = new LinkedList<>();
        for (Class<? extends BaseController> controllerClass : controllerClasses) {
            if (!Modifier.isAbstract(controllerClass.getModifiers())) {
                BaseController controller = getController(controllerClass);
                controllers.add(controller);
            }
        }

        return new ControllerRegistry(controllers);
    }

    private BaseController getController(Class<? extends BaseController> controllerClass)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        BaseController controller;
        if (ResourceUpsert.class.isAssignableFrom(controllerClass)) {
            Constructor<? extends BaseController> declaredConstructor = controllerClass
                    .getDeclaredConstructor(ResourceRegistry.class, TypeParser.class, ObjectMapper.class);
            controller = declaredConstructor.newInstance(resourceRegistry, typeParser, objectMapper);
        } else if (ResourceIncludeField.class.isAssignableFrom(controllerClass)) {
            Constructor<? extends BaseController> declaredConstructor = controllerClass
                    .getDeclaredConstructor(ResourceRegistry.class, TypeParser.class, IncludeLookupSetter.class);
            controller = declaredConstructor.newInstance(resourceRegistry, typeParser, includeFieldSetter);
        } else {
            Constructor<? extends BaseController> declaredConstructor = controllerClass
                    .getDeclaredConstructor(ResourceRegistry.class, TypeParser.class);
            controller = declaredConstructor.newInstance(resourceRegistry, typeParser);
        }
        return controller;
    }
}
