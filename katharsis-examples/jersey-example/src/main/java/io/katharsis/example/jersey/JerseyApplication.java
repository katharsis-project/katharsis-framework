package io.katharsis.example.jersey;

import javax.inject.Singleton;
import javax.ws.rs.ApplicationPath;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.server.ResourceConfig;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.core.properties.KatharsisProperties;
import io.katharsis.example.jersey.domain.repository.ProjectRepository;
import io.katharsis.example.jersey.domain.repository.TaskRepository;
import io.katharsis.example.jersey.domain.repository.TaskToProjectRepository;

@ApplicationPath("/")
public class JerseyApplication extends ResourceConfig {

    public static final String APPLICATION_URL = "http://localhost:8080";

    public JerseyApplication() {
        property(KatharsisProperties.RESOURCE_SEARCH_PACKAGE, "io.katharsis.example.jersey.domain");
        property(KatharsisProperties.RESOURCE_DEFAULT_DOMAIN, APPLICATION_URL);
        // if set to true, an empty JSON response will be returned instead of a 204 response without content
        property(KatharsisProperties.NULL_DATA_RESPONSE_ENABLED, false);
        register(KatharsisDynamicFeature.class);
        register(new AbstractBinder() {
            @Override
            public void configure() {
                bindFactory(ObjectMapperFactory.class).to(ObjectMapper.class).in(Singleton.class);
                bindService(TaskRepository.class);
                bindService(ProjectRepository.class);
                bindService(TaskToProjectRepository.class);
            }

            private void bindService(Class<?> serviceType) {
                bind(serviceType).to(serviceType).proxy(true).in(RequestScoped.class);
            }
        });

    }
}
