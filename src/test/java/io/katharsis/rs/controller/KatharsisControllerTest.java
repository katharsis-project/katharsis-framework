package io.katharsis.rs.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.locator.SampleJsonServiceLocator;
import io.katharsis.rs.KatharsisFeature;
import io.katharsis.rs.KatharsisProperties;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.ApplicationPath;
import java.net.URLEncoder;

import static io.katharsis.rs.type.JsonApiMediaType.APPLICATION_JSON_API_TYPE;
import static org.assertj.core.api.Assertions.assertThat;


public class KatharsisControllerTest extends JerseyTest {

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
    public void onSimpleCollectionGetShouldReturnCollectionOfResources() {
        // WHEN
        String taskCollectionResponse = target("tasks/")
                .request(APPLICATION_JSON_API_TYPE)
                .get(String.class);

        // THEN
        Assert.assertNotNull(taskCollectionResponse);
    }

    @Test
    public void onSimpleResourceGetShouldReturnOneResource() {
        // WHEN
        String taskResourceResponse = target("tasks/1")
                .queryParam("filter")
                .request(APPLICATION_JSON_API_TYPE)
                .get(String.class);

        // THEN
        Assert.assertNotNull(taskResourceResponse);
    }

    @Test
    public void onCollectionRequestWithParamsGetShouldReturnCollection() {
        // WHEN
        String taskResourceResponse = target("tasks")
                .queryParam("filter", URLEncoder.encode("{\"name\":\"John\"}"))
                .request(APPLICATION_JSON_API_TYPE)
                .get(String.class);

        // THEN
        Assert.assertNotNull(taskResourceResponse);
    }

    @Test
    public void onCollectionRequestWithPadramsGetShouldReturnCollection() {
        // WHEN
        String response = target("tasks/sample")
                .request()
                .get(String.class);

        // THEN
        assertThat(response).isEqualTo(SampleController.NON_KATHARSIS_RESOURCE_RESPONSE);
    }

    @ApplicationPath("/")
    private static class MyApplication extends ResourceConfig {
        public MyApplication() {
            property(KatharsisProperties.RESOURCE_SEARCH_PACKAGE, "io.katharsis.rs.resource");
            property(KatharsisProperties.RESOURCE_DEFAULT_DOMAIN, "http://test.local");
            register(SampleController.class);
            register(new KatharsisFeature(new ObjectMapper(), new SampleJsonServiceLocator()));

        }
    }
}
