package io.katharsis.dispatcher.controller;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.LinksRepository;
import io.katharsis.repository.MetaRepository;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.repository.adapter.RepositoryAdapter;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.JsonPath;
import io.katharsis.resource.exception.RequestBodyException;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.response.BaseResponse;
import io.katharsis.response.LinksInformation;
import io.katharsis.response.MetaInformation;

/**
 * Represents a controller contract. There can be many kinds of requests that can be send to the framework. The
 * initial process of checking if a request is acceptable is managed by
 * {@link BaseController#isAcceptable(io.katharsis.request.path.JsonPath, String)} method. If the method returns
 * true, the matched controller is used to handle the request.
 */
public interface BaseController {

    /**
     * Checks if requested resource method is acceptable.
     *
     * @param jsonPath    Requested resource path
     * @param requestType HTTP request type
     * @return Acceptance result in boolean
     */
    boolean isAcceptable(JsonPath jsonPath, String requestType);

    /**
     * Passes the request to controller method.
     *
     * @param jsonPath          Requested resource path
     * @param parameterProvider repository method parameter provider
     * @param queryParams       Params specifying request
     * @param requestBody       Top-level JSON object from method's body of the request passed as {@link RequestBody}
     * @return CollectionResponse object
     * @throws Exception internal Katharsis exception
     */
    BaseResponse<?> handle(JsonPath jsonPath, QueryParams queryParams, RepositoryMethodParameterProvider
        parameterProvider,
                           RequestBody requestBody) throws Exception;

    default MetaInformation getMetaInformation(Object repository, Iterable<?> resources, QueryParams queryParams) {
        if (repository instanceof RepositoryAdapter) {
            if (((RepositoryAdapter) repository).metaRepositoryAvailable()) {
                return ((MetaRepository) repository).getMetaInformation(resources, queryParams);
            }
        } else if (repository instanceof MetaRepository) {
            return ((MetaRepository) repository).getMetaInformation(resources, queryParams);
        }
        return null;
    }

    default LinksInformation getLinksInformation(Object repository, Iterable<?> resources, QueryParams queryParams) {
        if (repository instanceof RepositoryAdapter) {
            if (((RepositoryAdapter) repository).linksRepositoryAvailable()) {
                return ((LinksRepository) repository).getLinksInformation(resources, queryParams);
            }
        } else if (repository instanceof LinksRepository) {
            return ((LinksRepository) repository).getLinksInformation(resources, queryParams);
        }
        return null;
    }

    default void verifyTypes(HttpMethod methodType, String resourceEndpointName, RegistryEntry endpointRegistryEntry,
                             RegistryEntry bodyRegistryEntry) {
        if (endpointRegistryEntry.equals(bodyRegistryEntry)) {
            return;
        }
        if (!bodyRegistryEntry.isParent(endpointRegistryEntry)) {
            String message = String.format("Inconsistent type definition between path and body: body type: " +
                "%s, request type: %s", methodType, resourceEndpointName);
            throw new RequestBodyException(methodType, resourceEndpointName, message);
        }
    }
}
