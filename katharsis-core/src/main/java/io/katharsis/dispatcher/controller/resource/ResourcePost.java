package io.katharsis.dispatcher.controller.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.dispatcher.controller.Utils;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.request.Request;
import io.katharsis.request.dto.DataBody;
import io.katharsis.request.path.JsonApiPath;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.responseRepository.ResourceRepositoryAdapter;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.HttpStatus;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.response.ResourceResponseContext;
import io.katharsis.utils.ClassUtils;
import io.katharsis.utils.parser.TypeParser;

public class ResourcePost extends ResourceUpsert {

    public ResourcePost(ResourceRegistry resourceRegistry,
                        TypeParser typeParser,
                        QueryParamsBuilder paramsBuilder,
                        ObjectMapper objectMapper) {
        super(resourceRegistry, typeParser, paramsBuilder, objectMapper);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Check if it is a POST request for a resource.
     */
    @Override
    public boolean isAcceptable(Request request) {
        return request.getMethod() == HttpMethod.POST && request.getPath().isResource();
    }

    @Override
    public BaseResponseContext handle(Request request) {
        JsonApiPath path = request.getPath();

        RegistryEntry endpointRegistryEntry = resourceRegistry.getEntry(path.getResource());
        Utils.checkResourceExists(endpointRegistryEntry, path.getResource());

        QueryParams queryParams = getQueryParamsBuilder().parseQuery(request.getQuery());
        DataBody dataBody = dataBody(request);

        RegistryEntry bodyRegistryEntry = resourceRegistry.getEntry(dataBody.getType());

        verifyTypes(HttpMethod.POST, path.getResource(), endpointRegistryEntry, bodyRegistryEntry);

        Object newResource = ClassUtils.newInstance(bodyRegistryEntry.getResourceInformation().getResourceClass());

        setId(dataBody, newResource, bodyRegistryEntry);
        setAttributes(dataBody, newResource, bodyRegistryEntry.getResourceInformation());
        ResourceRepositoryAdapter resourceRepository = endpointRegistryEntry.getResourceRepository(request.getParameterProvider());
        setRelations(newResource, bodyRegistryEntry, dataBody, queryParams, request.getParameterProvider());
        JsonApiResponse response = resourceRepository.save(newResource, queryParams);

        return new ResourceResponseContext(response, path, queryParams, HttpStatus.CREATED_201);
    }

}
