package io.katharsis.core.internal.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.katharsis.core.internal.dispatcher.path.PathBuilder;
import io.katharsis.core.internal.utils.PropertyUtils;
import io.katharsis.legacy.queryParams.include.Inclusion;
import io.katharsis.legacy.queryParams.params.IncludedFieldsParams;
import io.katharsis.legacy.queryParams.params.IncludedRelationsParams;
import io.katharsis.legacy.queryParams.params.TypedParams;
import io.katharsis.repository.request.QueryAdapter;
import io.katharsis.resource.LinksContainer;
import io.katharsis.resource.MetaContainer;
import io.katharsis.resource.ResourceIdentifier;
import io.katharsis.resource.information.ResourceField;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.links.LinksInformation;
import io.katharsis.resource.links.RelatedLinksInformation;
import io.katharsis.resource.links.SelfLinksInformation;
import io.katharsis.resource.meta.MetaInformation;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;

public class DocumentMapperUtil {

	private ResourceRegistry resourceRegistry;

	private ObjectMapper objectMapper;

	public DocumentMapperUtil(ResourceRegistry resourceRegistry, ObjectMapper objectMapper) {
		this.resourceRegistry = resourceRegistry;
		this.objectMapper = objectMapper;
	}

	public String getRelationshipLink(ResourceInformation resourceInformation, Object entity, ResourceField field, boolean related) {
		String resourceUrl = resourceRegistry.getResourceUrl(resourceInformation);
		String resourceId = getIdString(entity, resourceInformation);
		return resourceUrl + "/" + resourceId + (!related ? "/" + PathBuilder.RELATIONSHIP_MARK + "/" : "/") + field.getJsonName();
	}

	public List<ResourceIdentifier> toResourceIds(Collection<?> entities) {
		List<ResourceIdentifier> results = new ArrayList<>();
		for (Object entity : entities) {
			results.add(toResourceId(entity));
		}
		return results;
	}

	public ResourceIdentifier toResourceId(Object entity) {
		if (entity == null) {
			return null;
		}
		RegistryEntry entry = resourceRegistry.findEntry(entity.getClass());
		ResourceInformation resourceInformation = entry.getResourceInformation();
		String strId = this.getIdString(entity, resourceInformation);
		return new ResourceIdentifier(strId, resourceInformation.getResourceType());
	}

	public String getIdString(Object entity, ResourceInformation resourceInformation) {
		ResourceField idField = resourceInformation.getIdField();
		Object sourceId = PropertyUtils.getProperty(entity, idField.getUnderlyingName());
		return resourceInformation.toIdString(sourceId);
	}

	protected static List<ResourceField> getRequestedFields(ResourceInformation resourceInformation, QueryAdapter queryAdapter, List<ResourceField> fields, boolean relation) {
		TypedParams<IncludedFieldsParams> includedFieldsSet = queryAdapter != null ? queryAdapter.getIncludedFields() : null;
		IncludedFieldsParams includedFields = includedFieldsSet != null ? includedFieldsSet.getParams().get(resourceInformation.getResourceType()) : null;

		if (noResourceIncludedFieldsSpecified(includedFields)) {
			return fields;
		} else {
			Set<String> includedFieldNames = includedFields.getParams();

			if (relation) {
				// for relations consider both "include" and "fields"
				TypedParams<IncludedRelationsParams> includedRelationsSet = queryAdapter.getIncludedRelations();
				IncludedRelationsParams includedRelations = includedRelationsSet != null ? includedRelationsSet.getParams().get(resourceInformation.getResourceType()) : null;
				if (includedRelations != null) {
					includedFieldNames = new HashSet<>(includedFieldNames);
					for (Inclusion include : includedRelations.getParams()) {
						includedFieldNames.add(include.getPath());
					}
				}
			}

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

	@JsonInclude(Include.NON_EMPTY)
	protected static class DefaultSelfRelatedLinksInformation implements SelfLinksInformation, RelatedLinksInformation {

		@JsonInclude(Include.NON_EMPTY)
		private String related;

		@JsonInclude(Include.NON_EMPTY)
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
		if (entity == null) {
			return null;
		}
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
		return resourceRegistry.findEntry(dataClass).getResourceInformation();
	}

	public ResourceInformation getResourceInformation(String resourceType) {
		return resourceRegistry.getEntry(resourceType).getResourceInformation();
	}

	public String getSelfUrl(ResourceInformation resourceInformation, Object entity) {
		String resourceUrl = resourceRegistry.getResourceUrl(resourceInformation);
		return resourceUrl + "/" + getIdString(entity, resourceInformation);
	}
}
