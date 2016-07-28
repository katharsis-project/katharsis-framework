package io.katharsis.example.wildfly;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.example.wildfly.serviceLocator.WildflyServiceLocator;
import io.katharsis.queryParams.DefaultQueryParamsParser;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.rs.KatharsisProperties;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

@Provider
public class KatharsisFeature implements Feature {
    public static final String APPLICATION_URL = "http://localhost:8080/wildfly-example";

    @Override
    public boolean configure(FeatureContext featureContext) {
        featureContext.property(KatharsisProperties.RESOURCE_SEARCH_PACKAGE, "io.katharsis.example.wildfly")
                .property(KatharsisProperties.RESOURCE_DEFAULT_DOMAIN, APPLICATION_URL)
                .register(new io.katharsis.rs.KatharsisFeature(new ObjectMapper(), new QueryParamsBuilder(new DefaultQueryParamsParser()), new WildflyServiceLocator()));

        return true;
    }
}
