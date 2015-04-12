package io.katharsis.rs.controller.hk2.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.jackson.exception.JsonDeserializationException;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.queryParams.RequestParams;
import org.glassfish.hk2.api.Factory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RequestParamsFactory implements Factory<RequestParams> {

    private final UriInfo uriInfo;
    private final ObjectMapper objectMapper;

    @Inject
    public RequestParamsFactory(Provider<UriInfo> uriInfoProvider, Provider<ObjectMapper> objectMapperProvider) {
        this.uriInfo = uriInfoProvider.get();
        this.objectMapper = objectMapperProvider.get();
    }

    @Override
    public RequestParams provide() {
        QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder(objectMapper);

        MultivaluedMap<String, String> queryParametersMultiMap = uriInfo.getQueryParameters();
        Map<String, String> queryParameters = new HashMap<>();

        for(String queryName : queryParametersMultiMap.keySet()) {
            queryParameters.put(queryName, queryParametersMultiMap.getFirst(queryName));
        }

        RequestParams requestParams;
        try {
            requestParams = queryParamsBuilder.buildRequestParams(queryParameters);
        } catch (JsonDeserializationException e) {
            throw new RuntimeException(e);
        }
        return requestParams;
    }

    @Override
    public void dispose(RequestParams requestParams) {

    }
}
