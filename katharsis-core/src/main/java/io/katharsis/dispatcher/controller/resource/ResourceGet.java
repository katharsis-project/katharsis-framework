package io.katharsis.dispatcher.controller.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.request.Request;
import io.katharsis.request.path.JsonApiPath;
import io.katharsis.resource.include.IncludeLookupSetter;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.responseRepository.ResourceRepositoryAdapter;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.response.ResourceResponseContext;
import io.katharsis.utils.parser.TypeParser;

import java.io.Serializable;
import java.util.List;

import static io.katharsis.dispatcher.controller.Utils.checkResourceExists;

public class ResourceGet extends ResourceIncludeField {

    public ResourceGet(ResourceRegistry resourceRegistry,
                       TypeParser typeParser,
                       IncludeLookupSetter fieldSetter,
                       QueryParamsBuilder paramsBuilder,
                       ObjectMapper objectMapper) {
        super(resourceRegistry, typeParser, fieldSetter, paramsBuilder, objectMapper);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Checks if requested resource method is acceptable - is a GET request for a resource.
     */
    @Override
    public boolean isAcceptable(Request request) {
        return request.getMethod() == HttpMethod.GET && !request.getPath().isCollection();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Passes the request to controller method.
     */
    @Override
    public BaseResponseContext handle(Request request) {
        JsonApiPath path = request.getPath();

        RegistryEntry registryEntry = resourceRegistry.getEntry(path.getResource());
        checkResourceExists(registryEntry, path.getResource());

        List<String> resourceIds = request.getPath().getIds().get();
        Serializable castedId = parseId(registryEntry, resourceIds.get(0));
        ResourceRepositoryAdapter resourceRepository = registryEntry.getResourceRepository(request.getParameterProvider());

        QueryParams params = getQueryParamsBuilder().parseQuery(path.getQuery());
        @SuppressWarnings("unchecked")
        JsonApiResponse response = resourceRepository.findOne(castedId, params);
        includeFieldSetter.injectIncludedRelationshipsInResource(response, request, params);

        return new ResourceResponseContext(response, path, params);
    }

}
