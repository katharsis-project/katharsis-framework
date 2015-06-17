package io.katharsis.rs.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.locator.SampleJsonServiceLocator;
import io.katharsis.response.HttpStatus;
import io.katharsis.rs.KatharsisFeature;
import io.katharsis.rs.KatharsisProperties;
import io.katharsis.rs.resource.exception.ExampleException;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.jetty.JettyTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URLEncoder;

import static io.katharsis.rs.type.JsonApiMediaType.APPLICATION_JSON_API_TYPE;
import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.assertj.core.api.Assertions.*;


public class KatharsisControllerTest extends JerseyTest {

    @Override
    protected TestContainerFactory getTestContainerFactory() {
        return new JettyTestContainerFactory();
    }

    @Override
    protected Application configure() {
        return new MyApplication();
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
    public void shouldReturnErrorResponseWhenMappedExceptionThrown() throws IOException {

        //Getting task of id = 5, simulates error and is throwing an exception we want to check.
        Response errorResponse = target("tasks/5")
                .request(APPLICATION_JSON_API_TYPE)
                .get();

        assertThat(errorResponse.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
        String errorBody = errorResponse.readEntity(String.class);
        assertThatJson(errorBody)
                .node("errors").isPresent()
                .node("errors[0].id").isStringEqualTo(ExampleException.ERROR_ID);
        assertThatJson(errorBody).node("errors[0].title").isStringEqualTo(ExampleException.ERROR_TITLE);
    }

    @Test
    public void onNonJsonApiRequestShouldReturnOk() {
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
