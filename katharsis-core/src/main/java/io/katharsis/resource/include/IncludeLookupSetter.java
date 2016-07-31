package io.katharsis.resource.include;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.include.Inclusion;
import io.katharsis.queryParams.params.IncludedRelationsParams;
import io.katharsis.queryParams.params.TypedParams;
import io.katharsis.repository.exception.RelationshipRepositoryNotFoundException;
import io.katharsis.request.Request;
import io.katharsis.resource.annotations.JsonApiLookupIncludeAutomatically;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.responseRepository.RelationshipRepositoryAdapter;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.utils.ClassUtils;
import io.katharsis.utils.Generics;
import io.katharsis.utils.PropertyUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
public class IncludeLookupSetter {

    private final ResourceRegistry resourceRegistry;

    public IncludeLookupSetter(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }

    private static Set<Inclusion> extractIncludedFields(TypedParams<IncludedRelationsParams> queryParams,
                                                        String resourceName) {
        IncludedRelationsParams includedRelationsParams = null;
        for (Map.Entry<String, IncludedRelationsParams> entry : queryParams.getParams().entrySet()) {
            if (resourceName.equals(entry.getKey())) {
                includedRelationsParams = entry.getValue();
            }
        }
        return includedRelationsParams == null ? new HashSet<Inclusion>() : includedRelationsParams.getParams();
    }

    public void injectIncludedElementsForCollection(Object responseResources,
                                                    Request request,
                                                    QueryParams queryParams) {


        for (Object resource : (Iterable) resolveResource(responseResources)) {
            injectIncludedRelationshipsInResource(resource, request, queryParams);
        }
    }

    /**
     * Iterate over included fields. Include all relationships for each field belonging to this resource
     *
     * @param responseResource
     * @param request
     * @param queryParams
     */
    public void injectIncludedRelationshipsInResource(Object responseResource,
                                                      Request request,
                                                      QueryParams queryParams) {

        String resourceName = request.getPath().getResource();
        RegistryEntry resourceEntry = resourceRegistry.getEntry(resourceName);

        Set<Inclusion> includedRelations = extractIncludedFields(queryParams.getIncludedRelations(), resourceName);

        // iterate over all included fields
        for (Inclusion inclusion : includedRelations) {
            if (!inclusion.isNestedPath()) {
                includeRelationshipsForResource(resourceEntry, resolveResource(responseResource), request, inclusion, queryParams);

            } else {
                log.warn("Unsupported nested inclusion {}", request.getPath());
            }
        }
    }

    private Object resolveResource(Object repositoryResource) {
        Object resource;
        if (repositoryResource instanceof JsonApiResponse) {
            resource = ((JsonApiResponse) repositoryResource).getEntity();
        } else {
            resource = repositoryResource;
        }
        return resource;
    }

    void includeRelationshipsForResource(RegistryEntry resourceEntry,
                                         Object resource,
                                         Request request,
                                         Inclusion inclusion,
                                         QueryParams queryParams) {
        // resolve field
        String includedRelationName = inclusion.getPathList().get(0);

        String underlyingFieldName = underlyingFieldName(resourceEntry, includedRelationName);

        Field field = ClassUtils.findClassField(resource.getClass(), underlyingFieldName);

        if (field == null) {
            log.warn("Error loading relationship, couldn't find field {}: {}", underlyingFieldName, request.getPath());
            return;
        }

        Object property = PropertyUtils.getProperty(resource, field.getName());
        //attempt to load relationship if it's null or JsonApiLookupIncludeAutomatically.overwrite() == true
        if (shouldWeLoadRelationship(field, property)) {
            property = loadRelationship(resource, field, queryParams);
            PropertyUtils.setProperty(resource, field.getName(), property);
        }

//        if (property != null) {
//            List<String> subPathList = pathList.subList(1, pathList.size());
//            if (isCollectionResource(property)) {
//                for (Object o : ((Iterable) property)) {
//                    //noinspection unchecked
//                    getElements(resourceEntry, o, subPathList, queryParams);
//                }
//            } else {
//                //noinspection unchecked
//                getElements(resourceEntry, property, subPathList, queryParams);
//            }
//        }
    }


    private String underlyingFieldName(RegistryEntry registryEntry, String fieldName) {
        String cleanedUpName = removeSurroundingBracketsAndQuotes(fieldName);
        ResourceField resourceField = registryEntry.getResourceInformation().findRelationshipFieldByName(cleanedUpName);
        return resourceField.getUnderlyingName();
    }

    private String removeSurroundingBracketsAndQuotes(String fieldName) {
        String result = removePrefix(fieldName, "[");
        result = removeSuffix(result, "]");
        return result;
    }

    private String removePrefix(String source, String prefix) {
        if (source.startsWith(prefix)) {
            return source.substring(prefix.length());
        }
        return source;
    }

    private String removeSuffix(String source, String suffix) {
        if (source.length() <= suffix.length()) {
            throw new IllegalStateException("Cannot remove " + suffix + " from " + source);
        }
        if (source.endsWith(suffix)) {
            return source.substring(0, source.length() - suffix.length());
        }
        return source;
    }

    private boolean shouldWeLoadRelationship(Field field, Object property) {
        return field.isAnnotationPresent(JsonApiLookupIncludeAutomatically.class)
                && (property == null || field.getAnnotation(JsonApiLookupIncludeAutomatically.class).overwrite());
    }

    @SuppressWarnings("unchecked")
    Object loadRelationship(Object root, Field relationshipField, QueryParams queryParams) {
        Class<?> resourceClass = getClassFromField(relationshipField);
        RegistryEntry<?> rootEntry = resourceRegistry.getEntry(root.getClass());
        RegistryEntry<?> registryEntry = resourceRegistry.getEntry(resourceClass);

        if (rootEntry == null || registryEntry == null) {
            return null;
        }

        ResourceField rootIdField = rootEntry.getResourceInformation().getIdField();
        Serializable castedResourceId = (Serializable) PropertyUtils.getProperty(root, rootIdField.getUnderlyingName());

        Class<?> baseRelationshipFieldClass = relationshipField.getType();
        Class<?> relationshipFieldClass = Generics.getResourceClass(root.getClass(), resourceClass);

        try {
            RelationshipRepositoryAdapter relationshipRepositoryForClass = rootEntry
                    .getRelationshipRepositoryForClass(relationshipFieldClass, null);
            if (relationshipRepositoryForClass != null) {
                JsonApiResponse response;
                if (Iterable.class.isAssignableFrom(baseRelationshipFieldClass)) {
                    response = relationshipRepositoryForClass.findManyTargets(castedResourceId, relationshipField.getName(), queryParams);
                } else {
                    response = relationshipRepositoryForClass.findOneTarget(castedResourceId, relationshipField.getName(), queryParams);
                }
                return response.getEntity();
            }
        } catch (RelationshipRepositoryNotFoundException e) {
            log.debug("Relationship is not defined", e);
        }

        return null;
    }

    Class<?> getClassFromField(Field relationshipField) {
        Class<?> resourceClass;
        if (Iterable.class.isAssignableFrom(relationshipField.getType())) {
            ParameterizedType stringListType = (ParameterizedType) relationshipField.getGenericType();
            resourceClass = (Class<?>) stringListType.getActualTypeArguments()[0];
        } else {
            resourceClass = relationshipField.getType();
        }
        return resourceClass;
    }
}
