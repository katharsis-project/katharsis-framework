package io.katharsis.rs.controller;

import io.katharsis.path.ResourcePath;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.CollectionResponse;
import io.katharsis.rs.controller.annotation.JsonInject;
import io.katharsis.rs.controller.hk2.JsonInjectResolver;
import io.katharsis.rs.controller.hk2.factory.ResourcePathFactory;
import io.katharsis.rs.controller.hk2.factory.ResourceRegistryFactory;
import io.katharsis.rs.resource.model.Task;
import io.katharsis.rs.resource.repository.ProjectRepository;
import io.katharsis.rs.resource.repository.TaskRepository;
import io.katharsis.rs.resource.repository.TaskToProjectRepository;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.Test;

import javax.inject.Singleton;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.GenericType;

public class JsonApiControllerTest extends JerseyTest {

    @Override
    protected TestContainerFactory getTestContainerFactory() {
        return new GrizzlyWebTestContainerFactory();
    }

    @Override
    protected DeploymentContext configureDeployment() {
        return ServletDeploymentContext.forServlet(
                new ServletContainer(new MyApplication())).build();
    }


    @Test
    public void onNestedPathShouldProcessIt() {
//        CollectionResponse<Task> clientResponse
        CollectionResponse<Task> taskCollectionResponse = target("tasks/")
                .request()
                .get(new GenericType<CollectionResponse<Task>>() {
                });

//        String s = clientResponse.readEntity(String.class);
//        assertEquals(Response.Status.OK.getStatusCode(), clientResponse.getStatus());
        taskCollectionResponse.equals(null);
    }

    @ApplicationPath("/")
    private static class MyApplication extends ResourceConfig {

        public MyApplication() {
            register(JsonApiController.class);
            register(new AbstractBinder() {
                @Override
                protected void configure() {
                    bind(ProjectRepository.class).to(ProjectRepository.class).in(Singleton.class);
                    bind(TaskRepository.class).to(TaskRepository.class).in(Singleton.class);
                    bind(TaskToProjectRepository.class).to(TaskToProjectRepository.class).in(Singleton.class);
                    bindFactory(ResourcePathFactory.class).to(ResourcePath.class).in(Singleton.class);
                    bindFactory(ResourceRegistryFactory.class).to(ResourceRegistry.class).in(Singleton.class);

                    bind(JsonInjectResolver.class)
                            .to(new TypeLiteral<InjectionResolver<JsonInject>>() {
                            })
                            .in(Singleton.class);
                }
            });
        }
    }
}
