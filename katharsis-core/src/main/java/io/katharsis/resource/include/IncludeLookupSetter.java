package io.katharsis.resource.include;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.katharsis.queryParams.include.Inclusion;
import io.katharsis.queryParams.params.IncludedRelationsParams;
import io.katharsis.queryParams.params.TypedParams;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.field.ResourceField.LookupIncludeBehavior;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.responseRepository.RelationshipRepositoryAdapter;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.utils.PropertyUtils;

public class IncludeLookupSetter {

	private static final Logger logger = LoggerFactory.getLogger(IncludeLookupSetter.class);

	private final ResourceRegistry resourceRegistry;

	public IncludeLookupSetter(ResourceRegistry resourceRegistry) {
		this.resourceRegistry = resourceRegistry;
	}

	@SuppressWarnings("rawtypes")
	public void setIncludedElements(String resourceName, Object repositoryResource, QueryAdapter queryAdapter,
			RepositoryMethodParameterProvider parameterProvider) {
		Object resource;
		if (repositoryResource instanceof JsonApiResponse) {
			resource = ((JsonApiResponse) repositoryResource).getEntity();
		}
		else {
			resource = repositoryResource;
		}
		if (resource != null && queryAdapter != null && queryAdapter.hasIncludedRelations()) {
			Iterable resources = resource instanceof Iterable ? (Iterable<?>) resource : Arrays.asList(resource);

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
	private void setIncludedElements(ResourceInformation resourceInformation, Iterable resources, List<String> pathList,
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

					setIncludedElements(propertyResourceInformation, (Iterable) properties, subPathList, queryAdapter,
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
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List filterResourcesForLookup(Iterable resources, ResourceField field) {
		List results = new ArrayList();
		Iterator iterator = resources.iterator();
		while (iterator.hasNext()) {
			Object resource = iterator.next();

			Object property = PropertyUtils.getProperty(resource, field.getUnderlyingName());
			LookupIncludeBehavior lookupIncludeBehavior = field.getLookupIncludeAutomatically();
			//attempt to load relationship if it's null or JsonApiLookupIncludeAutomatically.overwrite() == true
			if (lookupIncludeBehavior == LookupIncludeBehavior.AUTOMATICALLY_ALWAYS
					|| (property == null && lookupIncludeBehavior == LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL)) {
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
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Set loadRelationships(ResourceInformation resourceInformation, List resources, ResourceField relationshipField,
			QueryAdapter queryAdapter, RepositoryMethodParameterProvider parameterProvider) {
		RegistryEntry rootEntry = resourceRegistry.getEntry(resourceInformation.getResourceType());

		List<Serializable> resourceIds = getIds(resources, resourceInformation);

		boolean isMany = Iterable.class.isAssignableFrom(relationshipField.getType());
		Class<?> relationshipFieldClass = relationshipField.getElementType();

		Set loadedEntities = new HashSet();

		RelationshipRepositoryAdapter relationshipRepository = rootEntry.getRelationshipRepositoryForClass(relationshipFieldClass,
				parameterProvider);
		if (relationshipRepository != null) {
			Map<Object, JsonApiResponse> responseMap;
			if (isMany) {
				responseMap = relationshipRepository.findBulkManyTargets(resourceIds, relationshipField.getUnderlyingName(),
						queryAdapter);
			}
			else {
				responseMap = relationshipRepository.findBulkOneTargets(resourceIds, relationshipField.getUnderlyingName(),
						queryAdapter);
			}

			for (Object resource : resources) {
				ResourceField rootIdField = resourceInformation.getIdField();
				Serializable id = (Serializable) PropertyUtils.getProperty(resource, rootIdField.getUnderlyingName());
				JsonApiResponse response = responseMap.get(id);
				if (response != null) {
					// set the relation
					Object entity = response.getEntity();
					PropertyUtils.setProperty(resource, relationshipField.getUnderlyingName(), entity);

					addAll(loadedEntities, entity);
				}else{
					// null the relation
					PropertyUtils.setProperty(resource, relationshipField.getUnderlyingName(), null);
				}
			}
		}

		return loadedEntities;
	}

	private void addAll(Set<Object> set, Object entity) {
		if (entity instanceof Iterable) {
			Iterator<?> iterator = ((Iterable<?>) entity).iterator();
			while (iterator.hasNext()) {
				set.add(iterator.next());
			}
		}
		else {
			set.add(entity);
		}
	}

	@SuppressWarnings("rawtypes")
	private List<Serializable> getIds(List resources, ResourceInformation resourceInformation) {
		ResourceField rootIdField = resourceInformation.getIdField();
		List<Serializable> ids = new ArrayList<>();
		for (Object resource : resources) {
			Serializable id = (Serializable) PropertyUtils.getProperty(resource, rootIdField.getUnderlyingName());
			ids.add(id);
		}
		return ids;
	}

	private Class<?> getClassFromField(ResourceField relationshipField) {
		Class<?> resourceClass;
		if (Iterable.class.isAssignableFrom(relationshipField.getType())) {
			ParameterizedType stringListType = (ParameterizedType) relationshipField.getGenericType();
			resourceClass = (Class<?>) stringListType.getActualTypeArguments()[0];
		}
		else {
			resourceClass = relationshipField.getType();
		}
		return resourceClass;
	}
}
