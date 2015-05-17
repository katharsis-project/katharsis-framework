package io.katharsis.dispatcher.controller.resource;

import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.queryParams.RequestParams;
import io.katharsis.request.dto.DataBody;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.exception.RequestBodyNotFoundException;
import io.katharsis.resource.exception.ResourceNotFoundException;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.Container;
import io.katharsis.response.ResourceResponse;
import io.katharsis.utils.parser.TypeParser;
import org.apache.commons.beanutils.PropertyUtils;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

public class ResourcePost extends ResourceUpsert {

    public ResourcePost(ResourceRegistry resourceRegistry, TypeParser typeParser) {
        super(resourceRegistry, typeParser);
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
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        String resourceName = jsonPath.getResourceName();
        RegistryEntry registryEntry = resourceRegistry.getEntry(resourceName);
        if (registryEntry == null) {
            throw new ResourceNotFoundException(resourceName);
        }
        if (requestBody == null) {
            throw new RequestBodyNotFoundException(HttpMethod.POST, resourceName);
        }

        Object resource = buildNewResource(registryEntry, requestBody, resourceName);
        setAttributes(requestBody, resource);
        Object savedResource = registryEntry.getResourceRepository().save(resource);
        saveRelations(savedResource, registryEntry, requestBody);

        Serializable resourceId = (Serializable) PropertyUtils.getProperty(savedResource, registryEntry.getResourceInformation()
                .getIdField().getName());

        Object savedResourceWithRelations = registryEntry.getResourceRepository().findOne(resourceId);

        return new ResourceResponse(new Container(savedResourceWithRelations));
    }

    private Object buildNewResource(RegistryEntry registryEntry, RequestBody requestBody, String resourceName)
            throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        DataBody data = requestBody.getData();
        if (data == null) {
            throw new RuntimeException("No data field in the body.");
        }
        if (!resourceName.equals(data.getType())) {
            throw new RuntimeException(String.format("Inconsistent type definition between path and body: body type: " +
                    "%s, request type: %s", data.getType(), resourceName));
        }
        Object resource = registryEntry.getResourceInformation().getResourceClass().newInstance();
        return resource;
    }
}
