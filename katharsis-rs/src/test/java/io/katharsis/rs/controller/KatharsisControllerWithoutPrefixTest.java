package io.katharsis.rs.controller;

import static org.assertj.core.api.Assertions.assertThat;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.jetty.JettyTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.core.properties.KatharsisProperties;
import io.katharsis.legacy.locator.SampleJsonServiceLocator;
import io.katharsis.legacy.queryParams.DefaultQueryParamsParser;
import io.katharsis.legacy.queryParams.QueryParamsBuilder;
import io.katharsis.rs.KatharsisFeature;

public class KatharsisControllerWithoutPrefixTest extends KatharsisControllerTest {

    @Override
    protected TestContainerFactory getTestContainerFactory() {
        return new JettyTestContainerFactory();
    }

    @Override
    protected Application configure() {
        return new TestApplication();
    }

    @Override protected String getPrefix() {
        return null;
    }

    @ApplicationPath("/")
    private static class TestApplication extends ResourceConfig {
        public TestApplication() {
            property(KatharsisProperties.RESOURCE_SEARCH_PACKAGE, "io.katharsis.rs.resource");
            property(KatharsisProperties.RESOURCE_DEFAULT_DOMAIN, "http://test.local");
            register(SampleControllerWithoutPrefix.class);
            register(SampleOverlayingController.class);
            register(new KatharsisFeature(new ObjectMapper(), new QueryParamsBuilder(new DefaultQueryParamsParser()), new SampleJsonServiceLocator()));

        }
    }

    @Test
    public void onNonJsonApiPostCallShouldBeIgnored() {
        // WHEN
        Response response = target("tasks/1")
                .request(MediaType.MEDIA_TYPE_WILDCARD)
                .post(Entity.entity("binary", MediaType.APPLICATION_OCTET_STREAM_TYPE));

        // THEN
        assertThat(response.getStatusInfo().getFamily()).isEqualTo(Response.Status.Family.SUCCESSFUL);
        String responseString = response.readEntity(String.class);
        assertThat(responseString).isEqualTo(SampleOverlayingController.NON_KATHARSIS_RESOURCE_OVERLAY_RESPONSE);
    }

    @Test
    public void onNonJsonApiGetCallShouldBeIgnored() {
        // WHEN
        String response = target("tasks/1")
                .request(MediaType.TEXT_PLAIN_TYPE)
                .get(String.class);

        // THEN
        assertThat(response).isEqualTo(SampleOverlayingController.NON_KATHARSIS_RESOURCE_OVERLAY_RESPONSE);
    }
}
