package io.katharsis.core.internal.exception;

import io.katharsis.errorhandling.mapper.ExceptionMapper;
import io.katharsis.errorhandling.mapper.JsonApiExceptionMapper;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.reflect.TypeUtils;

public final class ExceptionMapperRegistryBuilder {
    private final Set<ExceptionMapperType> exceptionMappers = new HashSet<>();

    public ExceptionMapperRegistry build(String resourceSearchPackage) {
        return build(new DefaultExceptionMapperLookup(resourceSearchPackage));
    }

    public ExceptionMapperRegistry build(ExceptionMapperLookup exceptionMapperLookup) {
        addKatharsisDefaultMappers();
        for (JsonApiExceptionMapper<?> exceptionMapper : exceptionMapperLookup.getExceptionMappers()) {
            registerExceptionMapper(exceptionMapper);
        }
        return new ExceptionMapperRegistry(exceptionMappers);
    }

    private void addKatharsisDefaultMappers() {
        registerExceptionMapper(new KatharsisExceptionMapper());
    }

    private void registerExceptionMapper(JsonApiExceptionMapper<? extends Throwable> exceptionMapper) {
        Class<? extends Throwable> exceptionClass = getGenericType(exceptionMapper.getClass());
        exceptionMappers.add(new ExceptionMapperType(exceptionClass, exceptionMapper));
    }

    private Class<? extends Throwable> getGenericType(Class<? extends JsonApiExceptionMapper> mapper) {
      Type[] types = mapper.getGenericInterfaces();
      if (null == types || 0 == types.length ){
        types = new Type[]{mapper.getGenericSuperclass()};
      }

      for (Type type : types) {
        if (type instanceof ParameterizedType && (TypeUtils.isAssignable(((ParameterizedType) type).getRawType(),JsonApiExceptionMapper.class)
            || TypeUtils.isAssignable(((ParameterizedType) type).getRawType(),ExceptionMapper.class))) {
          //noinspection unchecked
          return (Class<? extends Throwable>) ((ParameterizedType) type).getActualTypeArguments()[0];
        }
      }
      //Won't get in here
      return null;
    }

}
