package io.katharsis.resource.include;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.include.Inclusion;
import io.katharsis.queryParams.params.IncludedRelationsParams;
import io.katharsis.queryParams.params.TypedParams;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.repository.exception.RelationshipRepositoryNotFoundException;
import io.katharsis.resource.annotations.JsonApiLookupIncludeAutomatically;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.field.ResourceField.LookupIncludeBehavior;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.responseRepository.RelationshipRepositoryAdapter;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.utils.ClassUtils;
import io.katharsis.utils.Generics;
import io.katharsis.utils.PropertyUtils;
import io.katharsis.utils.java.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;

public class IncludeLookupSetter {
    private static final transient Logger logger = LoggerFactory.getLogger(IncludeLookupSetter.class);

    private final ResourceRegistry resourceRegistry;

    public IncludeLookupSetter(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }

    public void setIncludedElements(String resourceName, Object repositoryResource, QueryAdapter queryAdapter,
                                    RepositoryMethodParameterProvider parameterProvider) {
        Object resource;
        if (repositoryResource instanceof JsonApiResponse) {
            resource = ((JsonApiResponse) repositoryResource).getEntity();
        } else {
            resource = repositoryResource;
        }
        if (resource != null && queryAdapter != null && queryAdapter.hasIncludedRelations()) {
            if (Iterable.class.isAssignableFrom(resource.getClass())) {
                for (Object target : (Iterable<?>) resource) {
                    setIncludedElements(resourceName, target, queryAdapter, parameterProvider);
                }
            } else {
                IncludedRelationsParams includedRelationsParams = findInclusions(queryAdapter.getIncludedRelations(),
                    resourceName);
                if (includedRelationsParams != null) {
                    for (Inclusion inclusion : includedRelationsParams.getParams()) {
                        List<String> pathList = inclusion.getPathList();
                        if (!pathList.isEmpty()) {
                            getElements(resource, pathList, queryAdapter, parameterProvider);
                        }
                    }
                }
            }
        }
    }

    private static IncludedRelationsParams findInclusions(TypedParams<IncludedRelationsParams> queryParams, String
        resourceName) {
        IncludedRelationsParams includedRelationsParams = null;
        for (Map.Entry<String, IncludedRelationsParams> entry : queryParams.getParams()
            .entrySet()) {
            if (resourceName.equals(entry.getKey())) {
                includedRelationsParams = entry.getValue();
            }
        }
        return includedRelationsParams;
    }

    private void getElements(Object resource, List<String> pathList, QueryAdapter queryAdapter,
                             RepositoryMethodParameterProvider parameterProvider) {
        if (!pathList.isEmpty()) {
        	
        	Optional<Class<?>> resourceClass = resourceRegistry.getResourceClass(resource);
        	ResourceField field = null;
        	if(resourceClass.isPresent()){
        		RegistryEntry entry = resourceRegistry.getEntry(resourceClass.get());
        		ResourceInformation resourceInformation = entry.getResourceInformation();
        		field = resourceInformation.findRelationshipFieldByName(pathList.get(0));
        	}
        	
            if (field == null) {
                logger.warn("Error loading relationship, couldn't find field " + pathList.get(0));
                return;
            }
            Object property = PropertyUtils.getProperty(resource, field.getUnderlyingName());
            LookupIncludeBehavior lookupIncludeBehavior = field.getLookupIncludeAutomatically();
            //attempt to load relationship if it's null or JsonApiLookupIncludeAutomatically.overwrite() == true
            if (lookupIncludeBehavior == LookupIncludeBehavior.AUTOMATICALLY_ALWAYS
                    || (property == null && lookupIncludeBehavior == LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL)) {
                property = loadRelationship(resource, field, queryAdapter, parameterProvider);
                PropertyUtils.setProperty(resource, field.getUnderlyingName(), property);
            }

            if (property != null) {
                List<String> subPathList = pathList.subList(1, pathList.size());
                if (Iterable.class.isAssignableFrom(property.getClass())) {
                    for (Object o : ((Iterable) property)) {
                        //noinspection unchecked
                        getElements(o, subPathList, queryAdapter, parameterProvider);
                    }
                } else {
                    //noinspection unchecked
                    getElements(property, subPathList, queryAdapter, parameterProvider);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Object loadRelationship(Object root, ResourceField relationshipField, QueryAdapter queryAdapter,
                                    RepositoryMethodParameterProvider parameterProvider) {
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
                .getRelationshipRepositoryForClass(relationshipFieldClass, parameterProvider);
            if (relationshipRepositoryForClass != null) {
                JsonApiResponse response;
                if (Iterable.class.isAssignableFrom(baseRelationshipFieldClass)) {
                    response = relationshipRepositoryForClass.findManyTargets(castedResourceId, relationshipField.getUnderlyingName(), queryAdapter);
                } else {
                    response = relationshipRepositoryForClass.findOneTarget(castedResourceId, relationshipField.getUnderlyingName(), queryAdapter);
                }
                return response.getEntity();
            }
        } catch (RelationshipRepositoryNotFoundException e) {
            logger.debug("Relationship is not defined", e);
        }

        return null;
    }

    private Class<?> getClassFromField(ResourceField relationshipField) {
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
