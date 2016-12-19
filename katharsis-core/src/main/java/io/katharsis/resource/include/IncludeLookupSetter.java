package io.katharsis.resource.include;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.katharsis.internal.boot.KatharsisBootProperties;
import io.katharsis.internal.boot.PropertiesProvider;
import io.katharsis.queryParams.include.Inclusion;
import io.katharsis.queryParams.params.IncludedRelationsParams;
import io.katharsis.queryParams.params.TypedParams;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.resource.Document;
import io.katharsis.resource.Relationship;
import io.katharsis.resource.Resource;
import io.katharsis.resource.ResourceId;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.field.ResourceField.LookupIncludeBehavior;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.repository.adapter.RelationshipRepositoryAdapter;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.utils.PropertyUtils;

public class IncludeLookupSetter {

    private static final Logger logger = LoggerFactory.getLogger(IncludeLookupSetter.class);

    private final ResourceRegistry resourceRegistry;
    private final PropertiesProvider propertiesProvider;
    private final LookupIncludeBehavior lookupIncludeBehavior;

    public IncludeLookupSetter(ResourceRegistry resourceRegistry, PropertiesProvider propertiesProvider) {
        this.resourceRegistry = resourceRegistry;
        this.propertiesProvider = propertiesProvider;

        if (propertiesProvider == null) {
            this.lookupIncludeBehavior = LookupIncludeBehavior.NONE;
            return;
        }
        // determine system property for include look up
        String includeAutomaticallyString = propertiesProvider.getProperty(KatharsisBootProperties.INCLUDE_AUTOMATICALLY);
        boolean includeAutomatically = Boolean.parseBoolean(includeAutomaticallyString);
        String includeAutomaticallyOverwriteString = propertiesProvider.getProperty(KatharsisBootProperties.INCLUDE_AUTOMATICALLY_OVERWRITE);
        boolean includeAutomaticallyOverwrite = Boolean.parseBoolean(includeAutomaticallyOverwriteString);
        if (includeAutomatically) {
            if (includeAutomaticallyOverwrite)
                lookupIncludeBehavior = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS;
            else
                lookupIncludeBehavior = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL;
        } else {
            lookupIncludeBehavior = LookupIncludeBehavior.NONE;
        }

    }

    @SuppressWarnings("rawtypes")
    public void setIncludedElements(String resourceName, Document document, QueryAdapter queryAdapter,
                                    RepositoryMethodParameterProvider parameterProvider) {
        Object data = document.getData();
        if (data != null && queryAdapter != null && queryAdapter.hasIncludedRelations()) {
            @SuppressWarnings("unchecked")
			Iterable<Resource> resources = (Iterable<Resource>) (data instanceof Iterable ?  data : Collections.singletonList(data));

            IncludedRelationsParams includedRelationsParams = findInclusions(queryAdapter.getIncludedRelations(), resourceName);
            if (includedRelationsParams != null) {
                for (Inclusion inclusion : includedRelationsParams.getParams()) {
                    List<String> pathList = inclusion.getPathList();
                    if (!pathList.isEmpty()) {
                        RegistryEntry entry = resourceRegistry.getEntry(resourceName);
                        ResourceInformation resourceInformation = entry.getResourceInformation();
                        setIncludedElements(resourceInformation, resources, pathList, queryAdapter, parameterProvider);
                    }
                }
            }
        }
    }

    private static IncludedRelationsParams findInclusions(TypedParams<IncludedRelationsParams> queryParams, String resourceName) {
        IncludedRelationsParams includedRelationsParams = null;
        for (Map.Entry<String, IncludedRelationsParams> entry : queryParams.getParams().entrySet()) {
            if (resourceName.equals(entry.getKey())) {
                includedRelationsParams = entry.getValue();
            }
        }
        return includedRelationsParams;
    }

    @SuppressWarnings("rawtypes")
    private void setIncludedElements(ResourceInformation resourceInformation, Iterable<Resource> resources, List<String> pathList,
                                     QueryAdapter queryAdapter, RepositoryMethodParameterProvider parameterProvider) {
        if (!pathList.isEmpty()) {
            ResourceField field = resourceInformation.findRelationshipFieldByName(pathList.get(0));
            if (field == null) {
                logger.warn("Error loading relationship, couldn't find field " + pathList.get(0));
                return;
            }

            List pendingResources = filterResourcesForLookup(resources, field);
            if (!pendingResources.isEmpty()) {
                Set properties = loadRelationships(resourceInformation, pendingResources, field, queryAdapter, parameterProvider);

                if (!properties.isEmpty() && pathList.size() > 1) {
                    List<String> subPathList = pathList.subList(1, pathList.size());

                    Class<?> propertyReousrceType = field.getElementType();
                    RegistryEntry propertyResourceEntry = resourceRegistry.getEntry(propertyReousrceType);
                    ResourceInformation propertyResourceInformation = propertyResourceEntry.getResourceInformation();

                    setIncludedElements(propertyResourceInformation, properties, subPathList, queryAdapter,
                            parameterProvider);
                }
            }
        }
    }

    /**
     * Filter by resources that need lookup based on incusion behavior.
     *
     * @param resources
     * @param field
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private List filterResourcesForLookup(Iterable resources, ResourceField field) {
        List results = new ArrayList();
        for (Object resource : resources) {
            if (resource == null) {
                continue;
            }
            Object property = PropertyUtils.getProperty(resource, field.getUnderlyingName());
            LookupIncludeBehavior lookupIncludeBehavior = field.getLookupIncludeAutomatically();
            //attempt to load relationship if it's null or JsonApiLookupIncludeAutomatically.overwrite() == true
            if ((lookupIncludeBehavior == LookupIncludeBehavior.AUTOMATICALLY_ALWAYS || this.lookupIncludeBehavior == LookupIncludeBehavior.AUTOMATICALLY_ALWAYS)
                    || (property == null && ((lookupIncludeBehavior == LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL) || this.lookupIncludeBehavior == LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL))) {
                results.add(resource);
            }
        }
        return results;
    }

    /**
     * Loads all related resources for the given resources and relationship field.
     *
     * @param resources
     * @param relationshipField
     * @param queryAdapter
     * @param parameterProvider
     * @return list of all loaded properties. Collections are flattened into a single collection.
     */
    private Set<Resource> loadRelationships(ResourceInformation resourceInformation, List<Resource> resources, ResourceField relationshipField,
                                  QueryAdapter queryAdapter, RepositoryMethodParameterProvider parameterProvider) {
        RegistryEntry rootEntry = resourceRegistry.getEntry(resourceInformation.getResourceType());

        List<Serializable> resourceIds = getIds(resources, resourceInformation);

        boolean isMany = Iterable.class.isAssignableFrom(relationshipField.getType());
        Class<?> relationshipFieldClass = relationshipField.getElementType();

        Set<Resource> loadedEntities = new HashSet<>();

        RelationshipRepositoryAdapter relationshipRepository = rootEntry.getRelationshipRepositoryForClass(relationshipFieldClass,
                parameterProvider);
        if (relationshipRepository != null) {
            Map<Object, JsonApiResponse> responseMap;
            if (isMany) {
                responseMap = relationshipRepository.findBulkManyTargets(resourceIds, relationshipField.getUnderlyingName(),
                        queryAdapter);
            } else {
                responseMap = relationshipRepository.findBulkOneTargets(resourceIds, relationshipField.getUnderlyingName(),
                        queryAdapter);
            }

            for (Resource resource : resources) {
                Serializable id = resourceInformation.parseIdString(resource.getId());
                Document response = responseMap.get(id);
                if (response != null) {
                    // set the relation

                	String relationshipName = relationshipField.getJsonName();
                	Map<String, Relationship> relationships = resource.getRelationships();
                	Relationship relationship = relationships.get(relationshipName);
                	if(relationship == null){
                		relationship = new Relationship();
                		relationships.put(relationshipName, relationship);
                	}
                	
                	relationship.setData(ResourceId.fromData(response.getData()));

                    addAll(loadedEntities, response.getData());
                }
            }
        }

        return loadedEntities;
    }

    private void addAll(Set<Resource> set, Object entity) {
        if (entity instanceof Iterable) {
            Iterator<?> iterator = ((Iterable<?>) entity).iterator();
            while (iterator.hasNext()) {
                set.add((Resource)iterator.next());
            }
        } else {
            set.add((Resource)entity);
        }
    }

    private List<Serializable> getIds(List<Resource> resources, ResourceInformation resourceInformation) {
        List<Serializable> ids = new ArrayList<>();
        for (Resource resource : resources) {
            Serializable id = (Serializable) resourceInformation.parseIdString(resource.getId());
            ids.add(id);
        }
        return ids;
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
