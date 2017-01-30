package io.katharsis.example.jersey;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.legacy.locator.JsonServiceLocator;
import io.katharsis.legacy.queryParams.DefaultQueryParamsParser;
import io.katharsis.legacy.queryParams.QueryParamsBuilder;
import io.katharsis.rs.KatharsisFeature;
import org.glassfish.hk2.api.ServiceLocator;

import javax.inject.Inject;

public class KatharsisDynamicFeature extends KatharsisFeature {

    @Inject
    public KatharsisDynamicFeature(ObjectMapper objectMapper, final ServiceLocator ServiceLocator) {
        super(objectMapper, new QueryParamsBuilder(new DefaultQueryParamsParser()), new JsonServiceLocator() {
            @Override
            public <T> T getInstance(Class<T> clazz) {
                return ServiceLocator.getService(clazz);
            }
        });
    }
}
