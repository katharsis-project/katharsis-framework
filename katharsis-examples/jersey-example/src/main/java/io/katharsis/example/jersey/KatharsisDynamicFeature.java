package io.katharsis.example.jersey;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.locator.JsonServiceLocator;
import io.katharsis.queryParams.DefaultQueryParamsParser;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.rs.KatharsisFeature;
import org.glassfish.hk2.api.ServiceLocator;

import javax.inject.Inject;

public class KatharsisDynamicFeature extends KatharsisFeature {

    @Inject
    public KatharsisDynamicFeature(ObjectMapper objectMapper, ServiceLocator ServiceLocator) {
        super(objectMapper, new QueryParamsBuilder(new DefaultQueryParamsParser()), new JsonServiceLocator() {
            @Override
            public <T> T getInstance(Class<T> clazz) {
                return ServiceLocator.getService(clazz);
            }
        });
    }
}
