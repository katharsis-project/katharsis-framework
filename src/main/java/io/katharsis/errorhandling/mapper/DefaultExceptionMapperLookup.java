package io.katharsis.errorhandling.mapper;

import io.katharsis.resource.exception.init.InvalidResourceException;
import org.reflections.Reflections;

import java.util.HashSet;
import java.util.Set;

/**
 * Exception mapper lookup which scans the classpath for exception mappers which
 * are annotated with the {@link ExceptionMapperProvider} annotation.
 */
public class DefaultExceptionMapperLookup implements ExceptionMapperLookup {
    private String resourceSearchPackage;

    public DefaultExceptionMapperLookup(String resourceSearchPackage) {
        this.resourceSearchPackage = resourceSearchPackage;
    }

    @Override
    public Set<JsonApiExceptionMapper> getExceptionMappers() {
        Reflections reflections;
        if (resourceSearchPackage != null) {
            String[] packageNames = resourceSearchPackage.split(",");
            reflections = new Reflections(packageNames);
        } else {
            reflections = new Reflections(resourceSearchPackage);
        }
        Set<Class<?>> exceptionMapperClasses = reflections.getTypesAnnotatedWith(ExceptionMapperProvider.class);

        Set<JsonApiExceptionMapper> exceptionMappers = new HashSet<>();
        for (Class<?> exceptionMapperClazz : exceptionMapperClasses) {
            if (!JsonApiExceptionMapper.class.isAssignableFrom(exceptionMapperClazz)) {
                throw new InvalidResourceException(exceptionMapperClazz.getCanonicalName() + " is not an implementation of JsonApiExceptionMapper");
            }
            try {
                exceptionMappers.add((JsonApiExceptionMapper<? extends Throwable>) exceptionMapperClazz.newInstance());
            } catch (Exception e) {
                throw new InvalidResourceException(exceptionMapperClazz.getCanonicalName() + " can not be initialized", e);
            }
        }
        return exceptionMappers;
    }

}
