package io.katharsis.resource.include;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.katharsis.internal.boot.PropertiesProvider;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.resource.Document;
import io.katharsis.resource.Relationship;
import io.katharsis.resource.Resource;
import io.katharsis.resource.ResourceId;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.field.ResourceField.LookupIncludeBehavior;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.internal.DocumentMapperUtil;
import io.katharsis.resource.internal.ResourceMapper;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.repository.adapter.RelationshipRepositoryAdapter;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.utils.PreconditionUtil;
import io.katharsis.utils.PropertyUtils;
import io.katharsis.utils.java.Nullable;

public class IncludeLookupSetter {

	private static final Logger LOGGER = LoggerFactory.getLogger(IncludeLookupSetter.class);

	private final ResourceRegistry resourceRegistry;
	private final LookupIncludeBehavior globalLookupIncludeBehavior;

	private ResourceMapper resourceMapper;

	private IncludeLookupUtil util;

	public IncludeLookupSetter(ResourceRegistry resourceRegistry, ResourceMapper resourceMapper, PropertiesProvider propertiesProvider) {
		this.resourceMapper = resourceMapper;
		this.resourceRegistry = resourceRegistry;

		this.globalLookupIncludeBehavior = IncludeLookupUtil.getDefaultLookupIncludeBehavior(propertiesProvider);
		this.util = new IncludeLookupUtil(resourceRegistry);

	}

	public void setIncludedElements(Document document, Object entity, QueryAdapter queryAdapter, RepositoryMethodParameterProvider parameterProvider) {
		List<Object> entityList = DocumentMapperUtil.toList(entity);
		List<Resource> dataList = DocumentMapperUtil.toList(document.getData());
		Map<ResourceId, Resource> dataMap = new HashMap<>();
		Map<ResourceId, Object> entityMap = new HashMap<>();
		for (int i = 0; i < dataList.size(); i++) {
			Resource dataElement = dataList.get(i);
			ResourceId id = dataElement.toIdentifier();
			entityMap.put(id, entityList.get(i));
			dataMap.put(id, dataElement);
		}

		Map<ResourceId, Resource> resourceMap = new HashMap<>();
		resourceMap.putAll(dataMap);

		Set<ResourceId> inclusions = new HashSet<>();

		ArrayList<ResourceField> stack = new ArrayList<>();
		populate(dataList, inclusions, resourceMap, entityMap, stack, queryAdapter, parameterProvider);

		// no need to include resources included in the data section
		inclusions.removeAll(dataMap.keySet());

		// setup included section
		ArrayList<Resource> included = new ArrayList<>();
		for (ResourceId inclusionId : inclusions) {
			Resource includedResource = resourceMap.get(inclusionId);
			PreconditionUtil.assertNotNull("resource not found", includedResource);
			included.add(includedResource);
		}
		Collections.sort((List<? extends ResourceId>) included);
		LOGGER.debug("Extracted included resources {}", included.toString());
		document.setIncluded(included);
	}

	private void populate(Collection<Resource> dataList, Set<ResourceId> inclusions, Map<ResourceId, Resource> resourceMap, Map<ResourceId, Object> entityMap, List<ResourceField> fieldPath, QueryAdapter queryAdapter,
			RepositoryMethodParameterProvider parameterProvider) {

		if (dataList.isEmpty()) {
			return; // nothing to do
		}

		int index = fieldPath.size();
		if (index >= 42) {
			throw new IllegalStateException("42 nested inclusions reached, aborting");
		}

		Set<ResourceField> relationshipFields = util.getRelationshipFields(dataList);
		for (ResourceField resourceField : relationshipFields) {
			if (fieldPath.contains(resourceField)) {
				// cyclic dependencies/inclusions
				continue;
			}

			fieldPath.add(resourceField);

			ResourceInformation resourceInformation = resourceField.getResourceInformation();

			// only handle resources from the proper subtype where the
			// relationship is desired to be loaded
			List<Resource> resourcesWithField = util.filterByLoadedRelationship(util.filterByType(dataList, resourceInformation), resourceField);

			// note that @JsonApiIncludeByDefault only applied for primary
			// resource, not nested ones
			boolean isPrimaryResource = fieldPath.size() == 1;

			boolean includeRequested = util.isInclusionRequested(queryAdapter, fieldPath);
			boolean includeResources = includeRequested || resourceField.getIncludeByDefault() && isPrimaryResource;
			boolean includeRelationshipData = !resourceField.isLazy() || includeResources;

			if (includeRelationshipData) {
				// lookup resources by inspecting the POJOs in entityMap
				LookupIncludeBehavior fieldLookupIncludeBehavior = resourceField.getLookupIncludeAutomatically();

				Set<Resource> populatedResources;
				if (fieldLookupIncludeBehavior == LookupIncludeBehavior.AUTOMATICALLY_ALWAYS || globalLookupIncludeBehavior == LookupIncludeBehavior.AUTOMATICALLY_ALWAYS) {
					// lookup resources by making repository calls
					populatedResources = lookupRelationshipField(resourcesWithField, resourceField, queryAdapter, parameterProvider, resourceMap, entityMap);
				} else if (fieldLookupIncludeBehavior == LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL || globalLookupIncludeBehavior == LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL) {
					// try to populate from entities
					Set<Resource> extractedResources = extractRelationshipField(resourcesWithField, resourceField, queryAdapter, resourceMap, entityMap);

					// do lookups where relationship data is null
					Collection<Resource> resourcesForLookup = util.findResourcesWithoutRelationshipData(resourcesWithField, resourceField);
					Collection<Resource> lookedupResources = lookupRelationshipField(resourcesForLookup, resourceField, queryAdapter, parameterProvider, resourceMap, entityMap);

					populatedResources = util.union(lookedupResources, extractedResources);
				} else {
					// do not do any lookups
					populatedResources = extractRelationshipField(resourcesWithField, resourceField, queryAdapter, resourceMap, entityMap);
					
					// set relationship data to null for single-valued relation. extractRelationshipField cannot differentiate between null and not loaded.
					// It assume it is null and loaded. Otherwise an application can reconfigure the includeBehavior to make a lookup and be sure.
					if(!Iterable.class.isAssignableFrom(resourceField.getType())){
						Nullable<Object> emptyData = Nullable.nullValue();
						for(Resource resourceWithField : resourcesWithField){
							Relationship relationship = resourceWithField.getRelationships().get(resourceField.getJsonName());
							if(!relationship.getData().isPresent()){
								relationship.setData(emptyData);
							}
						}
					}
				}

				// add inclusions and do nested population if requested as such
				if (includeResources && !populatedResources.isEmpty()) {
					inclusions.addAll(util.toIds(populatedResources));
					populate(populatedResources, inclusions, resourceMap, entityMap, fieldPath, queryAdapter, parameterProvider);
				}
			}

			fieldPath.remove(fieldPath.size() - 1);
		}
	}

	/**
	 * No lookup specified for the field. Attempt to load relationship from
	 * original POJOs.
	 */
	private Set<Resource> extractRelationshipField(List<Resource> sourceResources, ResourceField relationshipField, QueryAdapter queryAdapter, Map<ResourceId, Resource> resourceMap, Map<ResourceId, Object> entityMap) {
		// TODO nullable support to differentiate between not loaded and null
		Set<Resource> loadedEntities = new HashSet<>();
		for (Resource sourceResource : sourceResources) {
			ResourceId id = sourceResource.toIdentifier();

			Object source = entityMap.get(id);
			if (source != null && !(source instanceof Resource)) {
				Object targetEntity = PropertyUtils.getProperty(source, relationshipField.getUnderlyingName());
				if (targetEntity == null) {
					continue;
				}
				List<Resource> targetIds = setupRelation(sourceResource, relationshipField, targetEntity, queryAdapter, resourceMap, entityMap);
				loadedEntities.addAll(targetIds);
			}
		}
		return loadedEntities;
	}

	/**
	 * Loads all related resources for the given resources and relationship
	 * field. It updates the relationship data of the source resources
	 * accordingly and returns the loaded resources for potential inclusion in
	 * the result document.
	 */
	@SuppressWarnings("unchecked")
	private Set<Resource> lookupRelationshipField(Collection<Resource> sourceResources, ResourceField relationshipField, QueryAdapter queryAdapter, RepositoryMethodParameterProvider parameterProvider,
			Map<ResourceId, Resource> resourceMap, Map<ResourceId, Object> entityMap) {
		if (sourceResources.isEmpty()) {
			return Collections.emptySet();
		}

		ResourceInformation resourceInformation = relationshipField.getResourceInformation();
		RegistryEntry<?> registyEntry = resourceRegistry.getEntry(resourceInformation.getResourceType());

		List<Serializable> resourceIds = getIds(sourceResources, resourceInformation);

		boolean isMany = Iterable.class.isAssignableFrom(relationshipField.getType());
		Class<?> relationshipFieldClass = relationshipField.getElementType();

		Set<Resource> loadedTargets = new HashSet<>();

		@SuppressWarnings("rawtypes")
		RelationshipRepositoryAdapter relationshipRepository = registyEntry.getRelationshipRepositoryForClass(relationshipFieldClass, parameterProvider);
		if (relationshipRepository != null) {
			Map<Object, JsonApiResponse> responseMap;
			if (isMany) {
				responseMap = relationshipRepository.findBulkManyTargets(resourceIds, relationshipField.getUnderlyingName(), queryAdapter);
			} else {
				responseMap = relationshipRepository.findBulkOneTargets(resourceIds, relationshipField.getUnderlyingName(), queryAdapter);
			}

			for (Resource sourceResource : sourceResources) {
				Serializable sourceId = resourceInformation.parseIdString(sourceResource.getId());
				JsonApiResponse targetResponse = responseMap.get(sourceId);
				if (targetResponse != null && targetResponse.getEntity() != null) {
					Object targetEntity = targetResponse.getEntity();

					List<Resource> targets = setupRelation(sourceResource, relationshipField, targetEntity, queryAdapter, resourceMap, entityMap);
					loadedTargets.addAll(targets);
				}else{
					Nullable<Object> emptyData = Nullable.of(Iterable.class.isAssignableFrom(relationshipField.getType()) ? (Object)Collections.emptyList() : null);
					Relationship relationship = sourceResource.getRelationships().get(relationshipField.getJsonName());
					relationship.setData(emptyData);
				}
			}
		}

		return loadedTargets;
	}

	private List<Resource> setupRelation(Resource sourceResource, ResourceField relationshipField, Object targetEntity, QueryAdapter queryAdapter, Map<ResourceId, Resource> resourceMap,
			Map<ResourceId, Object> entityMap) {
		// set the relation
		String relationshipName = relationshipField.getJsonName();
		Map<String, Relationship> relationships = sourceResource.getRelationships();
		Relationship relationship = relationships.get(relationshipName);
		if (targetEntity instanceof Iterable) {
			List<Resource> targets = new ArrayList<>();
			for (Object targetElement : (Iterable<?>) targetEntity) {
				Resource targetResource = mergeResource(targetElement, queryAdapter, resourceMap, entityMap);
				targets.add(targetResource);
			}
			relationship.setData(Nullable.of((Object)targets));
			return targets;
		} else {
			Resource targetResource = mergeResource(targetEntity, queryAdapter, resourceMap, entityMap);
			relationship.setData(Nullable.of((Object)targetResource.toIdentifier()));
			return Collections.singletonList(targetResource);
		}
	}

	private Resource mergeResource(Object targetEntity, QueryAdapter queryAdapter, Map<ResourceId, Resource> resourceMap, Map<ResourceId, Object> entityMap) {
		Resource targetResource = resourceMapper.toData(targetEntity, queryAdapter);
		ResourceId targetId = targetResource.toIdentifier();
		if (!resourceMap.containsKey(targetId)) {
			resourceMap.put(targetId, targetResource);
		} else {
			// TODO consider merging
			targetResource = resourceMap.get(targetId);
		}
		if (!(targetEntity instanceof Resource)) {
			entityMap.put(targetId, targetEntity);
		}
		return targetResource;
	}

	private List<Serializable> getIds(Collection<Resource> resources, ResourceInformation resourceInformation) {
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
