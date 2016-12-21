package io.katharsis.resource.include;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.katharsis.internal.boot.KatharsisBootProperties;
import io.katharsis.internal.boot.PropertiesProvider;
import io.katharsis.queryParams.include.Inclusion;
import io.katharsis.queryParams.params.IncludedRelationsParams;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.resource.Relationship;
import io.katharsis.resource.Resource;
import io.katharsis.resource.ResourceId;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.field.ResourceField.LookupIncludeBehavior;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;

public class IncludeLookupUtil {

	private ResourceRegistry resourceRegistry;

	public IncludeLookupUtil(ResourceRegistry resourceRegistry) {
		this.resourceRegistry = resourceRegistry;
	}

	public static LookupIncludeBehavior getDefaultLookupIncludeBehavior(PropertiesProvider propertiesProvider) {
		if (propertiesProvider == null) {
			return LookupIncludeBehavior.NONE;
		}
		// determine system property for include look up
		String includeAutomaticallyString = propertiesProvider.getProperty(KatharsisBootProperties.INCLUDE_AUTOMATICALLY);
		boolean includeAutomatically = Boolean.parseBoolean(includeAutomaticallyString);
		String includeAutomaticallyOverwriteString = propertiesProvider.getProperty(KatharsisBootProperties.INCLUDE_AUTOMATICALLY_OVERWRITE);
		boolean includeAutomaticallyOverwrite = Boolean.parseBoolean(includeAutomaticallyOverwriteString);
		if (includeAutomatically) {
			if (includeAutomaticallyOverwrite)
				return LookupIncludeBehavior.AUTOMATICALLY_ALWAYS;
			else
				return LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL;
		} else {
			return LookupIncludeBehavior.NONE;
		}
	}

	public Set<ResourceField> getRelationshipFields(Collection<Resource> resources) {
		Map<String, ResourceField> fieldMap = new HashMap<>();

		Set<String> processedTypes = new HashSet<>();

		for (Resource resource : resources) {
			process(resource.getType(), processedTypes, fieldMap);
		}

		return new HashSet<>(fieldMap.values());
	}

	private void process(String type, Set<String> processedTypes, Map<String, ResourceField> fieldMap) {
		if (!processedTypes.contains(type)) {
			processedTypes.add(type);

			RegistryEntry<?> entry = resourceRegistry.getEntry(type);
			ResourceInformation information = entry.getResourceInformation();

			ResourceInformation superInformation = getSuperInformation(information);
			if (superInformation != null) {
				process(superInformation.getResourceType(), processedTypes, fieldMap);
			}

			// TODO same relationship on multiple children
			for (ResourceField field : information.getRelationshipFields()) {
				String name = field.getUnderlyingName();
				if (!fieldMap.containsKey(name)) {
					fieldMap.put(name, field);
				}
			}
		}
	}

	// TODO proper super type information
	private ResourceInformation getSuperInformation(ResourceInformation information) {
		Class<?> resourceClass = information.getResourceClass();
		Class<?> superclass = resourceClass.getSuperclass();
		if (superclass == Object.class) {
			return null;
		}
		boolean hasSuperType = resourceRegistry.hasEntry(superclass);
		return hasSuperType ? resourceRegistry.getEntry(superclass).getResourceInformation() : null;
	}

	public List<Resource> filterByType(Collection<Resource> resources, ResourceInformation resourceInformation) {
		List<Resource> results = new ArrayList<>();
		for (Resource resource : resources) {
			if (isInstance(resourceInformation, resource)) {
				results.add(resource);
			}
		}
		return results;
	}

	private boolean isInstance(ResourceInformation resourceInformation, Resource resource) {
		if (resourceInformation.getResourceType().equals(resource.getType())) {
			return true;
		}

		ResourceInformation superInformation = getSuperInformation(resourceInformation);
		if (superInformation != null) {
			return isInstance(superInformation, resource);
		} else {
			return false;
		}
	}

	public boolean isInclusionRequested(QueryAdapter queryAdapter, List<ResourceField> fieldPath) {
		if (queryAdapter == null || queryAdapter.getIncludedRelations() == null || queryAdapter.getIncludedRelations().getParams() == null) {
			return false;
		}
		Map<String, IncludedRelationsParams> params = queryAdapter.getIncludedRelations().getParams();

		// we have to possibilities for inclusion: by type or dot notation
		for (int i = fieldPath.size() - 1; i >= 0; i--) {
			String path = toPath(fieldPath, i);
			ResourceInformation rootInformation = fieldPath.get(i).getResourceInformation();
			IncludedRelationsParams includedRelationsParams = params.get(rootInformation.getResourceType());
			if (includedRelationsParams != null && contains(includedRelationsParams, path)) {
				return true;
			}
		}
		return false;
	}

	private boolean contains(IncludedRelationsParams includedRelationsParams, String path) {
		String pathPrefix = path + ".";
		for (Inclusion inclusion : includedRelationsParams.getParams()) {
			if (inclusion.getPath().equals(path) || inclusion.getPath().startsWith(pathPrefix)) {
				return true;
			}
		}
		return false;
	}

	private String toPath(List<ResourceField> fieldPath, int offset) {
		StringBuilder builder = new StringBuilder();
		for (int i = offset; i < fieldPath.size(); i++) {
			ResourceField field = fieldPath.get(i);
			if (builder.length() > 0) {
				builder.append(".");
			}
			builder.append(field.getJsonName());
		}
		return builder.toString();
	}

	public List<Resource> sub(Collection<Resource> resourcesWithField, Collection<Resource> resourcesForLookup) {
		List<Resource> result = new ArrayList<>(resourcesWithField);
		result.removeAll(resourcesForLookup);
		return result;
	}

	public List<Resource> filterByLoadedRelationship(List<Resource> resources, ResourceField resourceField) {
		List<Resource> results = new ArrayList<>();
		for (Resource resource : resources) {
			if (resource.getRelationships().get(resourceField.getJsonName()) != null) {
				results.add(resource);
			}
		}
		return results;
	}

	public Set<ResourceId> toIds(Set<Resource> resources) {
		Set<ResourceId> results = new HashSet<>();
		for (Resource resource : resources) {
			results.add(resource.toIdentifier());
		}
		return results;
	}

	public Set<Resource> union(Collection<Resource> set0, Collection<Resource> set1) {
		Map<ResourceId, Resource> map = new HashMap<>();
		for (Resource resource : set0) {
			map.put(resource.toIdentifier(), resource);
		}
		for (Resource resource : set1) {
			map.put(resource.toIdentifier(), resource);
		}
		return new HashSet<>(map.values());
	}

	public List<Resource> findResourcesWithoutRelationshipData(List<Resource> resources, ResourceField resourceField) {
		List<Resource> results = new ArrayList<>();
		for (Resource resource : resources) {
			Relationship relationship = resource.getRelationships().get(resourceField.getJsonName());
			if (!relationship.getData().isPresent()) {
				results.add(resource);
			}
		}
		return results;
	}
}
