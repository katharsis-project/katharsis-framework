package io.katharsis.spring.boot;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.dispatcher.DefaultJsonApiDispatcher;
import io.katharsis.dispatcher.JsonApiDispatcher;
import io.katharsis.dispatcher.handlers.JsonApiDelete;
import io.katharsis.dispatcher.handlers.JsonApiGet;
import io.katharsis.dispatcher.handlers.JsonApiPatch;
import io.katharsis.dispatcher.handlers.JsonApiPost;
import io.katharsis.dispatcher.registry.api.RepositoryRegistry;
import io.katharsis.errorhandling.mapper.ExceptionMapperRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RequestDispatcherConfiguration {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RepositoryRegistry repositoryRegistry;

    @Autowired
    private ExceptionMapperRegistry exceptionMapperRegistry;

    @Bean
    public JsonApiDispatcher requestDispatcher() throws Exception {
        return new DefaultJsonApiDispatcher(
                new JsonApiGet(repositoryRegistry),
                new JsonApiPost(repositoryRegistry),
                new JsonApiPatch(repositoryRegistry),
                new JsonApiDelete(repositoryRegistry),
                exceptionMapperRegistry);
    }
}
