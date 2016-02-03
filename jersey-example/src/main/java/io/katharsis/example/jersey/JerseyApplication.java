package io.katharsis.example.jersey;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.locator.SampleJsonServiceLocator;
import io.katharsis.queryParams.DefaultQueryParamsParser;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.rs.KatharsisFeature;
import io.katharsis.rs.KatharsisProperties;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/")
public class JerseyApplication extends ResourceConfig {

    public static final String APPLICATION_URL = "http://localhost:8080/";

    public JerseyApplication() {
        property(KatharsisProperties.RESOURCE_SEARCH_PACKAGE, "io.katharsis.example.jersey.domain");
        property(KatharsisProperties.RESOURCE_DEFAULT_DOMAIN, APPLICATION_URL);
        register(new KatharsisFeature(new ObjectMapper(), new QueryParamsBuilder(new DefaultQueryParamsParser()), new SampleJsonServiceLocator()));

    }
}