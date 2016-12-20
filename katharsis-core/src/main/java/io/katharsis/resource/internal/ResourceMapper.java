package io.katharsis.resource.internal;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.katharsis.queryParams.include.Inclusion;
import io.katharsis.queryParams.params.IncludedRelationsParams;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.resource.Relationship;
import io.katharsis.resource.Resource;
import io.katharsis.resource.ResourceId;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.internal.DocumentMapperUtil.DefaultSelfRelatedLinksInformation;
import io.katharsis.response.LinksInformation;
import io.katharsis.response.MetaInformation;
import io.katharsis.response.SelfLinksInformation;
import io.katharsis.utils.PropertyUtils;

public class ResourceMapper {

	private static final String SELF_FIELD_NAME = "self";
	private static final String RELATED_FIELD_NAME = "related";

	private DocumentMapperUtil util;
	private boolean client;
	private ObjectMapper objectMapper;

	public ResourceMapper(DocumentMapperUtil util, boolean client, ObjectMapper objectMapper) {
		this.util = util;
		this.client = client;
		this.objectMapper = objectMapper;
	}

	public Object toData(Object entity, QueryAdapter queryAdapter) {
		if (entity instanceof ResourceId) {
			// Resource and ResourceId
			return entity;
		} else {
			// map resource objects
			Class<?> dataClass = entity.getClass();

			ResourceInformation resourceInformation = util.getResourceInformation(dataClass);

			Resource resource = new Resource();
			resource.setId(util.getIdString(entity, resourceInformation));
			resource.setType(resourceInformation.getResourceType());
			if (!client) {
				util.setLinks(resource, getResourceLinks(entity, resourceInformation));
				util.setMeta(resource, getResourceMeta(entity, resourceInformation));
			}
			setAttributes(resource, entity, resourceInformation, queryAdapter);
			setRelationships(resource, entity, resourceInformation, queryAdapter);
			return resource;
		}
	}

	private MetaInformation getResourceMeta(Object entity, ResourceInformation resourceInformation) {
		if (resourceInformation.getMetaFieldName() != null) {
			return (MetaInformation) PropertyUtils.getProperty(entity, resourceInformation.getMetaFieldName());
		}
		return null;
	}

	public LinksInformation getResourceLinks(Object entity, ResourceInformation resourceInformation) {
		LinksInformation info;
		if (resourceInformation.getLinksFieldName() != null) {
			info = (LinksInformation) PropertyUtils.getProperty(entity, resourceInformation.getLinksFieldName());
		} else {
			info = new DefaultSelfRelatedLinksInformation();
		}
		if (info instanceof SelfLinksInformation) {
			SelfLinksInformation self = (SelfLinksInformation) info;
			if (self.getSelf() == null) {
				self.setSelf(util.getSelfUrl(resourceInformation, entity));
			}
		}
		return info;
	}

	private void setAttributes(Resource resource, Object entity, ResourceInformation resourceInformation, QueryAdapter queryAdapter) {
		// fields parameter may further limit the number of fields
		List<ResourceField> fields = DocumentMapperUtil.getRequestedFields(resourceInformation, queryAdapter, resourceInformation.getAttributeFields().getFields(), false);

		// serialize the individual attributes
		for (ResourceField field : fields) {
			Object value = PropertyUtils.getProperty(entity, field.getUnderlyingName());
			JsonNode valueNode = objectMapper.valueToTree(value);
			resource.getAttributes().put(field.getJsonName(), valueNode);
		}
	}

	private void setRelationships(Resource resource, Object entity, ResourceInformation resourceInformation, QueryAdapter queryAdapter) {
		List<ResourceField> fields = DocumentMapperUtil.getRequestedFields(resourceInformation, queryAdapter, resourceInformation.getRelationshipFields(), true);
		for (ResourceField field : fields) {
			ObjectNode relationshipLinks = objectMapper.createObjectNode();
			relationshipLinks.put(SELF_FIELD_NAME, util.getRelationshipLink(resourceInformation, entity, field, false));
			relationshipLinks.put(RELATED_FIELD_NAME, util.getRelationshipLink(resourceInformation, entity, field, true));

			Relationship relationship = new Relationship();
			relationship.setLinks(relationshipLinks);
			resource.getRelationships().put(field.getUnderlyingName(), relationship);

			boolean includeData = getIncludeRelationshipData(resourceInformation, field, queryAdapter);
			if (includeData) {
				relationship.setData(getRelationshipData(entity, resourceInformation, field));
			}
		}
	}

	protected boolean getIncludeRelationshipData(ResourceInformation resourceInformation, ResourceField field, QueryAdapter queryAdapter) {
		if (field.getIncludeByDefault() || !field.isLazy()) {
			return true;
		}
		String resourceType = resourceInformation.getResourceType();
		IncludedRelationsParams includedRelationsParams = queryAdapter.getIncludedRelations() != null ? queryAdapter.getIncludedRelations().getParams().get(resourceType) : null;
		if (includedRelationsParams != null) {
			for (Inclusion inclusion : includedRelationsParams.getParams()) {
				if (inclusion.getPath().equals(field.getJsonName())) {
					return true;
				}
			}
		}
		return false;
	}

	private Object getRelationshipData(Object entity, ResourceInformation resourceInformation, ResourceField field) {
		Object propertyValue = PropertyUtils.getProperty(entity, field.getUnderlyingName());
		if (propertyValue instanceof Iterable) {
			ArrayList<ResourceId> data = new ArrayList<>();
			for (Object propertyElement : (Iterable<?>) propertyValue) {
				data.add(util.toResourceId(propertyElement));
			}
			return data;
		} else {
			return util.toResourceId(propertyValue);
		}
	}

}
