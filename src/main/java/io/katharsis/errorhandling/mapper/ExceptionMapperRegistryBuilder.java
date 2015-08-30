package io.katharsis.errorhandling.mapper;

import io.katharsis.resource.exception.init.InvalidResourceException;
import org.reflections.Reflections;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public final class ExceptionMapperRegistryBuilder {
    private final Set<ExceptionMapperType> exceptionMappers = new HashSet<>();


    public Set<ExceptionMapperType> getExceptionMappers() {
        return exceptionMappers;
    }

    public ExceptionMapperRegistry build(String resourceSearchPackage) throws IllegalAccessException, InstantiationException {
        addKatharsisDefaultMappers();
        scanForCustomMappers(resourceSearchPackage);
        return new ExceptionMapperRegistry(exceptionMappers);
    }

    private void addKatharsisDefaultMappers() {
        registerExceptionMapper(new KatharsisExceptionMapper());
    }

    private void scanForCustomMappers(String resourceSearchPackage) throws InstantiationException, IllegalAccessException {
        Reflections reflections = new Reflections(resourceSearchPackage);
        Set<Class<?>> exceptionMapperClasses = reflections.getTypesAnnotatedWith(ExceptionMapperProvider.class);

        for (Class<?> exceptionMapperClazz : exceptionMapperClasses) {
            if (!JsonApiExceptionMapper.class.isAssignableFrom(exceptionMapperClazz)) {
                throw new InvalidResourceException(exceptionMapperClazz.getCanonicalName() + " is not an implementation of JsonApiExceptionMapper");
            }
            registerExceptionMapper((JsonApiExceptionMapper<? extends Throwable>) exceptionMapperClazz.newInstance());
        }
    }

    private void registerExceptionMapper(JsonApiExceptionMapper<? extends Throwable> exceptionMapper) {
        Class<? extends Throwable> exceptionClass = getGenericType(exceptionMapper.getClass());
        exceptionMappers.add(new ExceptionMapperType(exceptionClass, exceptionMapper));
    }

    private Class<? extends Throwable> getGenericType(Class<? extends JsonApiExceptionMapper> mapper) {
        Type[] types = mapper.getGenericInterfaces();
        for (Type type : types) {
            if (type instanceof ParameterizedType && ((ParameterizedType)type).getRawType() == JsonApiExceptionMapper.class) {
                //noinspection unchecked
                return (Class<? extends Throwable>)((ParameterizedType)type).getActualTypeArguments()[0];
            }
        }
        //Won't get in here
        return null;
    }

}
