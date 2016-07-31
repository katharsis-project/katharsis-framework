package io.katharsis.dispatcher.controller.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.dispatcher.controller.BaseController;
import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.dispatcher.controller.Utils;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.request.Request;
import io.katharsis.request.dto.DataBody;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.JsonApiPath;
import io.katharsis.resource.exception.RequestBodyException;
import io.katharsis.resource.exception.RequestBodyNotFoundException;
import io.katharsis.resource.exception.ResourceFieldNotFoundException;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.responseRepository.RelationshipRepositoryAdapter;
import io.katharsis.resource.registry.responseRepository.ResourceRepositoryAdapter;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.HttpStatus;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.response.ResourceResponseContext;
import io.katharsis.utils.Generics;
import io.katharsis.utils.parser.TypeParser;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@Getter
public abstract class RelationshipsResourceUpsert extends BaseController {

    private final TypeParser typeParser;
    private final ResourceRegistry resourceRegistry;
    private final QueryParamsBuilder paramsBuilder;

    RelationshipsResourceUpsert(ResourceRegistry resourceRegistry,
                                TypeParser typeParser,
                                QueryParamsBuilder paramsBuilder,
                                ObjectMapper objectMapper) {
        super(objectMapper);
        this.resourceRegistry = resourceRegistry;
        this.typeParser = typeParser;
        this.paramsBuilder = paramsBuilder;

    }

    /**
     * HTTP method name
     *
     * @return HTTP method name
     */
    protected abstract HttpMethod method();

    /**
     * Processes To-Many field
     *
     * @param resource                       source resource
     * @param relationshipEntry              {@link Class} class of the relationship's id field
     * @param elementName                    field's name
     * @param dataBodies                     Data bodies with relationships
     * @param queryParams                    query params
     * @param relationshipRepositoryForClass Relationship repository
     */
    protected abstract void processToManyRelationship(Object resource, RegistryEntry relationshipEntry,
                                                      String elementName, Iterable<DataBody> dataBodies, QueryParams queryParams,
                                                      RelationshipRepositoryAdapter relationshipRepositoryForClass);

    /**
     * Processes To-One field
     *
     * @param resource                       source resource
     * @param relationshipEntry              {@link Class} class of the relationship's id field
     * @param elementName                    field's name
     * @param dataBody                       Data body with a relationship
     * @param queryParams                    query params
     * @param relationshipRepositoryForClass Relationship repository
     */
    protected abstract void processToOneRelationship(Object resource, RegistryEntry relationshipEntry,
                                                     String elementName, DataBody dataBody, QueryParams queryParams,
                                                     RelationshipRepositoryAdapter relationshipRepositoryForClass);

    @Override
    public boolean isAcceptable(Request request) {
        return (request.getMethod() == HttpMethod.POST ||
                request.getMethod() == HttpMethod.PATCH ||
                request.getMethod() == HttpMethod.DELETE) &&
                request.getPath().isRelationshipResource();
    }

    @Override
    public BaseResponseContext handle(Request request) {
        JsonApiPath path = request.getPath();
        RegistryEntry registryEntry = resourceRegistry.getEntry(path.getResource());
        Utils.checkResourceExists(registryEntry, path.getResource());

        RequestBody requestBody = checkRequestBodyExists(request);

        Serializable castedResourceId = getResourceId(registryEntry, path.getIds().get());
        ResourceField relationshipField = registryEntry.getResourceInformation()
                .findRelationshipFieldByName(path.getRelationship().get());

        if (relationshipField == null) {
            throw new ResourceFieldNotFoundException(path.getRelationship().get());
        }
        ResourceRepositoryAdapter resourceRepository = registryEntry.getResourceRepository(request.getParameterProvider());

        QueryParams queryParams = getQueryParamsBuilder().parseQuery(path.getQuery());

        @SuppressWarnings("unchecked")
        JsonApiResponse response = resourceRepository.findOne(castedResourceId, queryParams);
        Object resource = extractResource(response);

        Class<?> baseRelationshipFieldClass = relationshipField.getType();
        Class<?> relationshipFieldClass = Generics
                .getResourceClass(relationshipField.getGenericType(), baseRelationshipFieldClass);

        RegistryEntry relationshipEntry = resourceRegistry.getEntry(relationshipFieldClass);

        @SuppressWarnings("unchecked")
        RelationshipRepositoryAdapter relationshipRepositoryForClass = registryEntry
                .getRelationshipRepositoryForClass(relationshipFieldClass, request.getParameterProvider());

        if (Iterable.class.isAssignableFrom(baseRelationshipFieldClass)) {
            if (!requestBody.isMultiple()) {
                throw new RequestBodyException(HttpMethod.POST, path.getResource(), "Non-multiple data in body");
            }
            Iterable<DataBody> dataBodies = requestBody.getMultipleData();
            processToManyRelationship(resource, relationshipEntry, path.getRelationship().get(), dataBodies, queryParams,
                    relationshipRepositoryForClass);
        } else {
            if (requestBody.isMultiple()) {
                throw new RequestBodyException(HttpMethod.POST, path.getResource(), "Multiple data in body");
            }
            DataBody dataBody = requestBody.getSingleData();
            processToOneRelationship(resource, relationshipEntry, path.getRelationship().get(), dataBody, queryParams,
                    relationshipRepositoryForClass);
        }

        return new ResourceResponseContext(response, HttpStatus.NO_CONTENT_204);
    }

    private RequestBody checkRequestBodyExists(Request request) {
        if (!request.getBody().isPresent()) {
            throw new RequestBodyNotFoundException(HttpMethod.POST, request.getPath().getResource());
        }
        return parseBody(request.getBody().get());
    }

    private Serializable getResourceId(RegistryEntry<?> registryEntry, List<String> ids) {
        String resourceId = ids.get(0);
        return parseId(registryEntry, resourceId);
    }

    @Override
    public TypeParser getTypeParser() {
        return typeParser;
    }

    @Override
    public QueryParamsBuilder getQueryParamsBuilder() {
        return paramsBuilder;
    }
}
