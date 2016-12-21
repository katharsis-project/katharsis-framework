package io.katharsis.resource.include;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
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

	private void populate(List<Resource> dataList, Set<ResourceId> inclusions, Map<ResourceId, Resource> resourceMap, Map<ResourceId, Object> entityMap, List<ResourceField> fieldPath, QueryAdapter queryAdapter,
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
			List<Resource> resourcesWithField = util.filterByType(dataList, resourceInformation);

			boolean includeRequested = util.isInclusionRequested(queryAdapter, fieldPath);
			boolean includeResources = includeRequested || resourceField.getIncludeByDefault();
			boolean includeRelationshipData = !resourceField.isLazy() || includeResources;

			if (includeRelationshipData) {
				List<Resource> resourcesForLookup = filterResourcesForLookup(resourcesWithField, resourceField);
				List<Resource> resourcesNotForLookup = util.sub(resourcesWithField, resourcesForLookup);

				Set<ResourceId> lookedupResourceIds = lookupRelationshipField(resourcesForLookup, resourceField, queryAdapter, parameterProvider, resourceMap, entityMap);
				Set<ResourceId> extractResourceIds = extractRelationshipField(resourcesNotForLookup, resourceField, queryAdapter, resourceMap, entityMap);
				if (includeResources) {
					inclusions.addAll(lookedupResourceIds);
					inclusions.addAll(extractResourceIds);
				}
			}

			// recurse
			populate(dataList, inclusions, resourceMap, entityMap, fieldPath, queryAdapter, parameterProvider);

			fieldPath.remove(fieldPath.size() - 1);
		}
	}

	/**
	 * Filter by resources that need lookup based on incusion behavior.
	 *
	 * @param resources
	 * @param field
	 */
	private List<Resource> filterResourcesForLookup(Iterable<Resource> resources, ResourceField field) {
		List<Resource> results = new ArrayList<>();
		for (Resource resource : resources) {
			Relationship relationship = resource.getRelationships().get(field.getJsonName());
			PreconditionUtil.assertNotNull("expected relationship to be initialized", relationship);
			Object relationshipData = relationship.getData();

			LookupIncludeBehavior fieldLookupIncludeBehavior = field.getLookupIncludeAutomatically();
			// attempt to load relationship if it's null or
			// JsonApiLookupIncludeAutomatically.overwrite() == true
			if ((fieldLookupIncludeBehavior == LookupIncludeBehavior.AUTOMATICALLY_ALWAYS || globalLookupIncludeBehavior == LookupIncludeBehavior.AUTOMATICALLY_ALWAYS)
					|| (relationshipData == null && ((fieldLookupIncludeBehavior == LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL) || globalLookupIncludeBehavior == LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL))) {
				results.add(resource);
			}
		}
		return results;
	}

	/**
	 * No lookup specified for the field. Attempt to load relationship from
	 * original POJOs.
	 */
	private Set<ResourceId> extractRelationshipField(List<Resource> sourceResources, ResourceField relationshipField, QueryAdapter queryAdapter, Map<ResourceId, Resource> resourceMap, Map<ResourceId, Object> entityMap) {
		Set<ResourceId> loadedEntities = new HashSet<>();
		for (Resource sourceResource : sourceResources) {
			ResourceId id = sourceResource.toIdentifier();

			Object source = entityMap.get(id);
			if (source != null && !(source instanceof Resource)) {
				Object targetEntity = PropertyUtils.getProperty(source, relationshipField.getJsonName());
				if (targetEntity == null) {
					continue;
				}
				List<ResourceId> targetIds = setupRelation(sourceResource, relationshipField, targetEntity, queryAdapter, resourceMap, entityMap);
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
	private Set<ResourceId> lookupRelationshipField(List<Resource> sourceResources, ResourceField relationshipField, QueryAdapter queryAdapter, RepositoryMethodParameterProvider parameterProvider,
			Map<ResourceId, Resource> resourceMap, Map<ResourceId, Object> entityMap) {

		ResourceInformation resourceInformation = relationshipField.getResourceInformation();
		RegistryEntry<?> registyEntry = resourceRegistry.getEntry(resourceInformation.getResourceType());

		List<Serializable> resourceIds = getIds(sourceResources, resourceInformation);

		boolean isMany = Iterable.class.isAssignableFrom(relationshipField.getType());
		Class<?> relationshipFieldClass = relationshipField.getElementType();

		Set<ResourceId> loadedEntities = new HashSet<>();

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
				if (targetResponse != null) {
					Object targetEntity = targetResponse.getEntity();

					List<ResourceId> targetIds = setupRelation(sourceResource, relationshipField, targetEntity, queryAdapter, resourceMap, entityMap);
					loadedEntities.addAll(targetIds);
				}
			}
		}

		return loadedEntities;
	}

	private List<ResourceId> setupRelation(Resource sourceResource, ResourceField relationshipField, Object targetEntity, QueryAdapter queryAdapter, Map<ResourceId, Resource> resourceMap,
			Map<ResourceId, Object> entityMap) {
		// set the relation
		String relationshipName = relationshipField.getJsonName();
		Map<String, Relationship> relationships = sourceResource.getRelationships();
		Relationship relationship = relationships.get(relationshipName);
		if (relationship == null) {
			throw new IllegalStateException();
			// relationship = new Relationship();
			// relationships.put(relationshipName, relationship);
		}

		if (targetEntity instanceof Iterable) {
			List<ResourceId> targetIds = new ArrayList<>();
			for (Object targetElement : (Iterable<?>) targetEntity) {
				Resource targetResource = mergeResource(targetElement, queryAdapter, resourceMap, entityMap);
				targetIds.add(targetResource.toIdentifier());
			}
			relationship.setData(targetIds);
			return targetIds;
		} else {
			Resource targetResource = mergeResource(targetEntity, queryAdapter, resourceMap, entityMap);
			relationship.setData(targetResource.toIdentifier());
			return Collections.singletonList(targetResource.toIdentifier());
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
