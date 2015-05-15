package io.katharsis.errorHandling;

import io.katharsis.errorHandling.mappers.RepositoryNotFoundExceptionMapper;
import org.reflections.Reflections;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public class ExceptionMapperRegistryBuilder {
    private final Set<ExceptionMapperType> exceptionMappers = new HashSet<>();


    public Set<ExceptionMapperType> getExceptionMappers() {
        return exceptionMappers;
    }

    public ExceptionMapperRegistry build(String resourceSearchPackage) {
        addKatharsisDefaultMappers();
        scanForCustomMappers(resourceSearchPackage);
        return new ExceptionMapperRegistry(exceptionMappers);
    }

    void addKatharsisDefaultMappers() {
        registerExceptionMapper(new RepositoryNotFoundExceptionMapper());
    }

    void scanForCustomMappers(String resourceSearchPackage) {
        Reflections reflections = new Reflections(resourceSearchPackage);
        Set<Class<?>> exceptionMapperClasses = reflections.getTypesAnnotatedWith(ExceptionMapperProvider.class);

        for (Class<?> exceptionMapperClazz : exceptionMapperClasses) {
            if (!JsonApiExceptionMapper.class.isAssignableFrom(exceptionMapperClazz)) {
                throw new IllegalStateException("Not an implementation of JsonApiExceptionClass");
            }
            try {
                registerExceptionMapper((JsonApiExceptionMapper<? extends Throwable>) exceptionMapperClazz.newInstance());
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void registerExceptionMapper(JsonApiExceptionMapper<? extends Throwable> exceptionMapper) {
        Class<? extends Throwable> exceptionClass = getGenericType(exceptionMapper.getClass());
        exceptionMappers.add(new ExceptionMapperType(exceptionClass, exceptionMapper));
    }

    Class<? extends Throwable> getGenericType(Class<? extends JsonApiExceptionMapper> mapper) {
        Type[] types = mapper.getGenericInterfaces();
        for (Type type : types) {
            if (type instanceof ParameterizedType && ((ParameterizedType)type).getRawType() == JsonApiExceptionMapper.class) {
                return (Class<? extends Throwable>)((ParameterizedType)type).getActualTypeArguments()[0];
            }
        }
        //Won't get in here
        return null;
    }

}
