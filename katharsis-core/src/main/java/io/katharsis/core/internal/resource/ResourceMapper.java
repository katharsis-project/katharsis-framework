package io.katharsis.core.internal.resource;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.katharsis.core.internal.resource.DocumentMapperUtil.DefaultSelfRelatedLinksInformation;
import io.katharsis.core.internal.utils.PropertyUtils;
import io.katharsis.repository.request.QueryAdapter;
import io.katharsis.resource.Relationship;
import io.katharsis.resource.Resource;
import io.katharsis.resource.information.ResourceField;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.links.LinksInformation;
import io.katharsis.resource.links.SelfLinksInformation;
import io.katharsis.resource.meta.MetaInformation;

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

	public Resource toData(Object entity, QueryAdapter queryAdapter) {
		if (entity instanceof Resource) {
			// Resource and ResourceId
			return (Resource) entity;
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
		if (resourceInformation.getMetaField() != null) {
			return (MetaInformation) PropertyUtils.getProperty(entity, resourceInformation.getMetaField().getUnderlyingName());
		}
		return null;
	}

	public LinksInformation getResourceLinks(Object entity, ResourceInformation resourceInformation) {
		LinksInformation info;
		if (resourceInformation.getLinksField() != null) {
			info = (LinksInformation) PropertyUtils.getProperty(entity, resourceInformation.getLinksField().getUnderlyingName());
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

	protected void setAttributes(Resource resource, Object entity, ResourceInformation resourceInformation, QueryAdapter queryAdapter) {
		// fields parameter may further limit the number of fields
		List<ResourceField> fields = DocumentMapperUtil.getRequestedFields(resourceInformation, queryAdapter, resourceInformation.getAttributeFields().getFields(), false);

		// serialize the individual attributes
		for (ResourceField field : fields) {
			Object value = PropertyUtils.getProperty(entity, field.getUnderlyingName());
			JsonNode valueNode = objectMapper.valueToTree(value);
			resource.getAttributes().put(field.getJsonName(), valueNode);
		}
	}

	protected void setRelationships(Resource resource, Object entity, ResourceInformation resourceInformation, QueryAdapter queryAdapter) {
		List<ResourceField> fields = DocumentMapperUtil.getRequestedFields(resourceInformation, queryAdapter, resourceInformation.getRelationshipFields(), true);
		for (ResourceField field : fields) {
			setRelationship(resource, field, entity, resourceInformation, queryAdapter);
		}
	}

	protected void setRelationship(Resource resource, ResourceField field, Object entity, ResourceInformation resourceInformation, QueryAdapter queryAdapter) {
		ObjectNode relationshipLinks = objectMapper.createObjectNode();
		relationshipLinks.put(SELF_FIELD_NAME, util.getRelationshipLink(resourceInformation, entity, field, false));
		relationshipLinks.put(RELATED_FIELD_NAME, util.getRelationshipLink(resourceInformation, entity, field, true));

		Relationship relationship = new Relationship();
		relationship.setLinks(relationshipLinks);
		resource.getRelationships().put(field.getJsonName(), relationship);
	}
}
