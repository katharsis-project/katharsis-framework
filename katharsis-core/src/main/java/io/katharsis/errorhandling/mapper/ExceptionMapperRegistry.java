package io.katharsis.errorhandling.mapper;

import io.katharsis.utils.java.Optional;

import java.util.Set;

public final class ExceptionMapperRegistry {

    private final Set<ExceptionMapperType> exceptionMappers;

    ExceptionMapperRegistry(Set<ExceptionMapperType> exceptionMappers) {
        this.exceptionMappers = exceptionMappers;
    }

    Set<ExceptionMapperType> getExceptionMappers() {
        return exceptionMappers;
    }

    public Optional<JsonApiExceptionMapper> findMapperFor(Class<? extends Throwable> exceptionClass) {
        int currentDistance = Integer.MAX_VALUE;
        JsonApiExceptionMapper closestExceptionMapper = null;
        for (ExceptionMapperType mapperType : exceptionMappers) {
            int tempDistance = getDistanceBetweenExceptions(exceptionClass, mapperType.getExceptionClass());
            if (tempDistance < currentDistance) {
                currentDistance = tempDistance;
                closestExceptionMapper = mapperType.getExceptionMapper();
                if (currentDistance == 0) {
                    break;
                }
            }
        }
        return Optional.ofNullable(closestExceptionMapper);
    }

    int getDistanceBetweenExceptions(Class<?> clazz, Class<?> mapperTypeClazz) {
        int distance = 0;
        Class<?> superClazz = clazz;
        if (!mapperTypeClazz.isAssignableFrom(clazz))
            return Integer.MAX_VALUE;

        while (superClazz != mapperTypeClazz) {
            superClazz = superClazz.getSuperclass();
            distance++;
        }
        return distance;
    }

}