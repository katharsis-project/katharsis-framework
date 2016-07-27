package io.katharsis.rs.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.locator.SampleJsonServiceLocator;
import io.katharsis.queryParams.DefaultQueryParamsParser;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.rs.KatharsisFeature;
import io.katharsis.rs.KatharsisProperties;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.jetty.JettyTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

public class KatharsisControllerWithPrefixTest extends KatharsisControllerTest {
    private static final String PREFIX = "/api/v1";

    @Override
    protected TestContainerFactory getTestContainerFactory() {
        return new JettyTestContainerFactory();
    }

    @Override
    protected Application configure() {
        return new TestApplication();
    }

    @Override protected String getPrefix() {
        return PREFIX;
    }

    @ApplicationPath("/")
    private static class TestApplication extends ResourceConfig {
        public TestApplication() {
            property(KatharsisProperties.RESOURCE_SEARCH_PACKAGE, "io.katharsis.rs.resource");
            property(KatharsisProperties.RESOURCE_DEFAULT_DOMAIN, "http://test.local");
            property(KatharsisProperties.WEB_PATH_PREFIX, PREFIX);
            register(SampleControllerWithPrefix.class);
            register(new KatharsisFeature(new ObjectMapper(), new QueryParamsBuilder(new DefaultQueryParamsParser()), new SampleJsonServiceLocator()));

        }
    }
}
