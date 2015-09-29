package io.katharsis.dispatcher.controller.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.queryParams.RequestParams;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.request.dto.DataBody;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.exception.RequestBodyException;
import io.katharsis.resource.exception.RequestBodyNotFoundException;
import io.katharsis.resource.exception.ResourceNotFoundException;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.LinksInformation;
import io.katharsis.response.MetaInformation;
import io.katharsis.response.ResourceResponse;
import io.katharsis.utils.PropertyUtils;
import io.katharsis.utils.parser.TypeParser;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;

public class ResourcePost extends ResourceUpsert {

    public ResourcePost(ResourceRegistry resourceRegistry, TypeParser typeParser, ObjectMapper objectMapper) {
        super(resourceRegistry, typeParser, objectMapper);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Check if it is a POST request for a resource.
     */
    @Override
    public boolean isAcceptable(JsonPath jsonPath, String requestType) {
        return jsonPath.isCollection() &&
                jsonPath instanceof ResourcePath &&
                HttpMethod.POST.name().equals(requestType);
    }

    @Override
    public ResourceResponse handle(JsonPath jsonPath, RequestParams requestParams, RequestBody requestBody)
        throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException,
        IOException {
        String resourceName = jsonPath.getResourceName();
        RegistryEntry registryEntry = resourceRegistry.getEntry(resourceName);
        if (registryEntry == null) {
            throw new ResourceNotFoundException(resourceName);
        }
        if (requestBody == null) {
            throw new RequestBodyNotFoundException(HttpMethod.POST, resourceName);
        }
        if (requestBody.isMultiple()) {
            throw new RequestBodyException(HttpMethod.POST, resourceName, "Multiple data in body");
        }

        DataBody dataBody = requestBody.getSingleData();
        Object resource = buildNewResource(registryEntry, dataBody, resourceName);

        setAttributes(dataBody, resource, registryEntry.getResourceInformation());
        ResourceRepository resourceRepository = registryEntry.getResourceRepository();
        Object savedResource = resourceRepository.save(resource);
        saveRelations(savedResource, registryEntry, dataBody);

        Serializable resourceId = (Serializable) PropertyUtils
            .getProperty(savedResource, registryEntry.getResourceInformation().getIdField().getName());

        @SuppressWarnings("unchecked")
        Object savedResourceWithRelations = resourceRepository.findOne(resourceId, requestParams);
        MetaInformation metaInformation =
            getMetaInformation(resourceRepository, Collections.singletonList(savedResourceWithRelations), requestParams);
        LinksInformation linksInformation =
            getLinksInformation(resourceRepository, Collections.singletonList(savedResourceWithRelations), requestParams);

        return new ResourceResponse(savedResourceWithRelations, jsonPath, requestParams, metaInformation, linksInformation);
    }
}
