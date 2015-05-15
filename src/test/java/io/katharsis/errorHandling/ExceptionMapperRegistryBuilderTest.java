package io.katharsis.errorHandling;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.errorHandling.handlers.SomeExceptionMapper;
import io.katharsis.errorHandling.mappers.RepositoryNotFoundExceptionMapper;
import org.junit.Test;

import java.util.Optional;

public class ExceptionMapperRegistryBuilderTest {

    @Test
    public void shouldName() throws Exception {
        ExceptionMapperRegistryBuilder builder = new ExceptionMapperRegistryBuilder();
        builder.addKatharsisDefaultMappers();

        System.out.println(builder.getExceptionMappers());
    }

    @Test
    public void shouldabc() throws Exception {
        ExceptionMapperRegistryBuilder builder = new ExceptionMapperRegistryBuilder();
        builder.scanForCustomMappers("io.katharsis.errorHandling.handlers");

        System.out.println(builder.getExceptionMappers());

    }

    @Test
    public void shouldMap() throws Exception {
        ExceptionMapperRegistryBuilder builder = new ExceptionMapperRegistryBuilder();
        ExceptionMapperRegistry exceptionMapperRegistry = builder.build("io.katharsis.errorHandling.handlers");

        try {
            throw new SomeExceptionMapper.SomeException();
        } catch (Exception e) {
            Optional<JsonApiExceptionMapper> exceptionMapper = exceptionMapperRegistry.findMapperFor(e.getClass());
            if (exceptionMapper.isPresent()) {
                ErrorResponse errorResponse = exceptionMapper.get().toErrorResponse(e);
                System.out.println(new ObjectMapper().writeValueAsString(errorResponse));
            } else {
                throw e;
            }
        }
    }

    @Test
    public void shouldNamea() throws Exception {

        System.out.println(JsonApiExceptionMapper.class.isAssignableFrom(Object.class));
        System.out.println(JsonApiExceptionMapper.class.isAssignableFrom(RepositoryNotFoundExceptionMapper.class));

    }
}
