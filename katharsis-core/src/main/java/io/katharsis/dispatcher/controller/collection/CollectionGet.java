package io.katharsis.dispatcher.controller.collection;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.dispatcher.controller.resource.ResourceIncludeField;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.request.Request;
import io.katharsis.request.path.JsonApiPath;
import io.katharsis.resource.include.IncludeLookupSetter;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.responseRepository.ResourceRepositoryAdapter;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.CollectionResponseContext;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.utils.parser.TypeParser;

import java.io.Serializable;

import static io.katharsis.dispatcher.controller.Utils.checkResourceExists;

public class CollectionGet extends ResourceIncludeField {

    public CollectionGet(ResourceRegistry resourceRegistry,
                         TypeParser typeParser,
                         IncludeLookupSetter fieldSetter,
                         QueryParamsBuilder queryParamsBuilder,
                         ObjectMapper mapper) {
        super(resourceRegistry, typeParser, fieldSetter, queryParamsBuilder, mapper);
    }

    /**
     * Check if it is a GET request for a collection of resources.
     */
    @Override
    public boolean isAcceptable(Request request) {
        return request.getMethod() == HttpMethod.GET && request.getPath().isCollection();
    }

    @Override
    public BaseResponseContext handle(Request request) {
        JsonApiPath path = request.getPath();

        RegistryEntry registryEntry = resourceRegistry.getEntry(path.getResource());
        checkResourceExists(registryEntry, path.getResource());

        QueryParams queryParams = getQueryParamsBuilder().parseQuery(request.getQuery());
        ResourceRepositoryAdapter resourceRepository = registryEntry.getResourceRepository(request.getParameterProvider());

        JsonApiResponse response;
        if (path.getIds().isPresent()) {
            Iterable<? extends Serializable> parsedIds = request.getPath().getIds().get();
            response = resourceRepository.findAll(parsedIds, queryParams);
        } else {
            response = resourceRepository.findAll(queryParams);
        }

        if (queryParams.hasIncludedRelations()) {
            includeFieldSetter.injectIncludedElementsForCollection(response, request, queryParams);
        }

        return new CollectionResponseContext(response, path, queryParams);
    }

}
