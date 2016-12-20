package io.katharsis.resource.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.katharsis.queryParams.include.Inclusion;
import io.katharsis.queryParams.params.IncludedRelationsParams;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.resource.Resource;
import io.katharsis.resource.ResourceId;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.utils.PropertyUtils;

/**
 * Extracts inclusions from a resource.
 */
public class IncludedRelationshipExtractor {

	private static final Logger LOGGER = LoggerFactory.getLogger(IncludedRelationshipExtractor.class);

	private DocumentMapperUtil util;

	private ResourceMapper resourceMapper;

	public IncludedRelationshipExtractor(DocumentMapperUtil util, ResourceMapper resourceMapper) {
		this.resourceMapper = resourceMapper;
		this.util = util;
	}

	/**
	 * Return a list of resources that are included with the document.
	 * 
	 * Inclusions happen if:
	 * <ul>
	 * <li>property is annotated with
	 * {@link io.katharsis.resource.annotations.JsonApiIncludeByDefault} down
	 * the chain.</li>
	 * <li>included by the <i>include</i> query parameter.
	 * include=projects.task.project.</li>
	 * </ul>
	 */
	public List<Resource> extractIncludedResources(List<Resource> data, List<Object> entities, QueryAdapter queryAdapter) {
		if (data.size() != entities.size()) {
			throw new IllegalStateException();
		}
		Map<ResourceId, Resource> dataMap = new HashMap<>();
		Map<ResourceId, Object> entityMap = new HashMap<>();
		for (int i = 0; i < data.size(); i++) {
			Resource dataElement = data.get(i);
			ResourceId id = dataElement.toIdentifier();
			entityMap.put(id, entities.get(i));
			dataMap.put(id, dataElement);
		}

		Map<ResourceId, Resource> resourceMap = new HashMap<>();
		resourceMap.putAll(dataMap);

		for (Object entity : entities) {
			populateIncludedResources(entity, resourceMap, 0, queryAdapter);
		}

		// no need to include resources included in the data section
		for (ResourceId id : dataMap.keySet()) {
			resourceMap.remove(id);
		}

		ArrayList<Resource> included = new ArrayList<>(resourceMap.values());
		Collections.sort((List<? extends ResourceId>)included);
		LOGGER.debug("Extracted included resources {}", included.toString());
		return included;
	}

	private void populateIncludedResources(Object resource, Map<ResourceId, Resource> resourceMap, int index, QueryAdapter queryAdapter) {
		if (index >= 42) {
			throw new IllegalStateException("nested inclusions reach 42 nested inclusions, aborting");
		}

		ResourceInformation resourceInformation = util.getResourceInformation(resource.getClass());

		ResourceId resourceId = util.toResourceId(resource);
		if (!resourceMap.containsKey(resourceId)) {
			resourceMap.put(resourceId, (Resource) resourceMapper.toData(resource, queryAdapter));
		}

		List<ResourceField> relationshipFields = resourceInformation.getRelationshipFields();
		for (ResourceField resourceField : relationshipFields) {
			if (resourceField.getIncludeByDefault() || isFieldIncluded(queryAdapter, resourceField, index, resourceInformation)) {

				Object targetItems = PropertyUtils.getProperty(resource, resourceField.getJsonName());
				if (targetItems == null) {
					continue;
				}
				for (Object targetItem : DocumentMapperUtil.toList(targetItems)) {
					ResourceId targetId = util.toResourceId(targetItem);
					if (targetId == null || resourceMap.containsKey(targetId)) {
						// null or given item was already processed => ignore
						continue;
					}
					populateIncludedResources(targetItem, resourceMap, index + 1, queryAdapter);
				}
			}
		}
	}

	private boolean isFieldIncluded(QueryAdapter queryAdapter, ResourceField field, int index, ResourceInformation resourceInformation) {
		if (queryAdapter == null || queryAdapter.getIncludedRelations() == null || queryAdapter.getIncludedRelations().getParams() == null) {
			return false;
		}
		IncludedRelationsParams includedRelationsParams = queryAdapter.getIncludedRelations().getParams().get(resourceInformation.getResourceType());
		if (includedRelationsParams == null || includedRelationsParams.getParams() == null) {
			return false;
		}

		String fieldName = field.getJsonName();
		for (Inclusion inclusion : includedRelationsParams.getParams()) {
			if (inclusion.getPathList().size() > index && inclusion.getPathList().get(index).equals(fieldName)) {
				return true;
			}
		}

		return false;
	}
}
