package io.katharsis.rs.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.locator.SampleJsonServiceLocator;
import io.katharsis.rs.KatharsisFeature;
import io.katharsis.rs.KatharsisProperties;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.jetty.JettyTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

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
            register(new KatharsisFeature(new ObjectMapper(), new SampleJsonServiceLocator()));

        }
    }
}
