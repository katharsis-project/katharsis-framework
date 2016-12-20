package io.katharsis.resource.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.katharsis.queryParams.params.IncludedFieldsParams;
import io.katharsis.queryParams.params.TypedParams;
import io.katharsis.request.path.PathBuilder;
import io.katharsis.resource.LinksContainer;
import io.katharsis.resource.MetaContainer;
import io.katharsis.resource.ResourceId;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.LinksInformation;
import io.katharsis.response.MetaInformation;
import io.katharsis.response.RelatedLinksInformation;
import io.katharsis.response.SelfLinksInformation;
import io.katharsis.utils.PropertyUtils;

public class DocumentMapperUtil {

	private ResourceRegistry resourceRegistry;

	private ObjectMapper objectMapper;

	public DocumentMapperUtil(ResourceRegistry resourceRegistry, ObjectMapper objectMapper) {
		this.resourceRegistry = resourceRegistry;
		this.objectMapper = objectMapper;
	}

	public String getRelationshipLink(ResourceInformation resourceInformation, Object entity, ResourceField field, boolean related) {
		String resourceUrl = resourceRegistry.getResourceUrl(resourceInformation.getResourceClass());
		String resourceId = getIdString(entity, resourceInformation);
		return resourceUrl + "/" + resourceId + (related ? "/" + PathBuilder.RELATIONSHIP_MARK + "/" : "/") + field.getJsonName();
	}

	public ResourceId toResourceId(Object entity) {
		if (entity == null) {
			return null;
		}
		RegistryEntry<Object> entry = resourceRegistry.getEntry(entity);
		ResourceInformation resourceInformation = entry.getResourceInformation();
		String strId = this.getIdString(entity, resourceInformation);
		return new ResourceId(strId, resourceInformation.getResourceType());
	}

	public String getIdString(Object entity, ResourceInformation resourceInformation) {
		ResourceField idField = resourceInformation.getIdField();
		Object sourceId = PropertyUtils.getProperty(entity, idField.getUnderlyingName());
		return resourceInformation.toIdString(sourceId);
	}

	protected static List<ResourceField> getRequestedFields(ResourceInformation resourceInformation, TypedParams<IncludedFieldsParams> includedFieldsSet, List<ResourceField> fields) {
		IncludedFieldsParams includedFields = includedFieldsSet.getParams().get(resourceInformation.getResourceType());

		if (noResourceIncludedFieldsSpecified(includedFields)) {
			return fields;
		} else {
			Set<String> includedFieldNames = includedFields.getParams();

			List<ResourceField> results = new ArrayList<>();
			for (ResourceField field : fields) {
				if (includedFieldNames.contains(field.getJsonName())) {
					results.add(field);
				}
			}
			return results;
		}
	}

	public void setLinks(LinksContainer container, LinksInformation linksInformation) {
		if (linksInformation != null) {
			container.setLinks((ObjectNode) objectMapper.valueToTree(linksInformation));
		}
	}

	public void setMeta(MetaContainer container, MetaInformation metaInformation) {
		if (metaInformation != null) {
			container.setMeta((ObjectNode) objectMapper.valueToTree(metaInformation));
		}
	}

	protected static boolean noResourceIncludedFieldsSpecified(IncludedFieldsParams typeIncludedFields) {
		return typeIncludedFields == null || typeIncludedFields.getParams().isEmpty();
	}

	protected static class DefaultSelfRelatedLinksInformation implements SelfLinksInformation, RelatedLinksInformation {

		private String related;
		private String self;

		@Override
		public String getRelated() {
			return related;
		}

		@Override
		public void setRelated(String related) {
			this.related = related;
		}

		@Override
		public String getSelf() {
			return self;
		}

		@Override
		public void setSelf(String self) {
			this.self = self;
		}

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> List<T> toList(Object entity) {
		if (entity instanceof List) {
			return (List) entity;
		} else if (entity instanceof Iterable) {
			ArrayList<T> result = new ArrayList<>();
			for (Object element : (Iterable) entity) {
				result.add((T) element);
			}
			return result;
		} else {
			return Collections.singletonList((T) entity);
		}
	}

	public ResourceInformation getResourceInformation(Class<?> dataClass) {
		return resourceRegistry.getEntry(dataClass).getResourceInformation();
	}

	public String getSelfUrl(ResourceInformation resourceInformation, Object entity) {
		String resourceUrl = resourceRegistry.getResourceUrl(resourceInformation.getResourceClass());
		return resourceUrl + "/" + getIdString(entity, resourceInformation);
	}
}
