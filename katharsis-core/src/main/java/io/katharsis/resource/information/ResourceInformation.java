package io.katharsis.resource.information;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import io.katharsis.core.internal.resource.DefaultResourceInstanceBuilder;
import io.katharsis.core.internal.resource.ResourceAttributesBridge;
import io.katharsis.core.internal.utils.PropertyUtils;
import io.katharsis.errorhandling.exception.MultipleJsonApiLinksInformationException;
import io.katharsis.errorhandling.exception.MultipleJsonApiMetaInformationException;
import io.katharsis.errorhandling.exception.ResourceDuplicateIdException;
import io.katharsis.errorhandling.exception.ResourceIdNotFoundException;
import io.katharsis.resource.Document;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.utils.parser.TypeParser;

/**
 * Holds information about the type of the resource.
 */
public class ResourceInformation {

	private final Class<?> resourceClass;

	/**
	 * Type name of the resource. Corresponds to {@link JsonApiResource.type}
	 * for annotated resources.
	 */
	private String resourceType;

	/**
	 * Found field of the id. Each resource has to contain a field marked by
	 * JsonApiId annotation.
	 */
	private final ResourceField idField;

	/**
	 * A set of resource's attribute fields.
	 */
	private final ResourceAttributesBridge attributeFields;

	/**
	 * A set of fields that contains non-standard Java types (List, Set, custom
	 * classes, ...).
	 */
	private final List<ResourceField> relationshipFields;

	/**
	 * An underlying field's name which contains meta information about for a
	 * resource
	 */
	private final ResourceField metaField;

	/**
	 * An underlying field's name which contain links information about for a
	 * resource
	 */
	private final ResourceField linksField;

	/**
	 * Creates a new instance of the given resource.
	 */
	private ResourceInstanceBuilder<?> instanceBuilder;

	private TypeParser parser;

	public ResourceInformation(TypeParser parser, Class<?> resourceClass, String resourceType, List<ResourceField> fields) {
		this(parser, resourceClass, resourceType, null, fields);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ResourceInformation(TypeParser parser, Class<?> resourceClass, String resourceType, ResourceInstanceBuilder<?> instanceBuilder, List<ResourceField> fields) {
		this.parser = parser;
		this.resourceClass = resourceClass;
		this.resourceType = resourceType;
		this.instanceBuilder = instanceBuilder;

		if (fields != null) {
			List<ResourceField> idFields = ResourceFieldType.ID.filter(fields);
			if (idFields.isEmpty()) {
				throw new ResourceIdNotFoundException(resourceClass.getCanonicalName());
			}
			if (idFields.size() > 1) {
				throw new ResourceDuplicateIdException(resourceClass.getCanonicalName());
			}

			this.idField = idFields.get(0);

			this.attributeFields = new ResourceAttributesBridge(ResourceFieldType.ATTRIBUTE.filter(fields), resourceClass);
			this.relationshipFields = ResourceFieldType.RELATIONSHIP.filter(fields);

			this.metaField = getMetaField(resourceClass, fields);
			this.linksField = getLinksField(resourceClass, fields);

			for (ResourceField resourceField : fields) {
				resourceField.setResourceInformation(this);
			}
		} else {
			this.relationshipFields = Collections.emptyList();
			this.attributeFields = new ResourceAttributesBridge(Collections.emptyList(), resourceClass);
			this.metaField = null;
			this.linksField = null;
			this.idField = null;
		}
		if (this.instanceBuilder == null) {
			this.instanceBuilder = new DefaultResourceInstanceBuilder(resourceClass);
		}
	}

	private static <T> ResourceField getMetaField(Class<T> resourceClass, Collection<ResourceField> classFields) {
		List<ResourceField> metaFields = new ArrayList<>(1);
		for (ResourceField field : classFields) {
			if (field.getResourceFieldType() == ResourceFieldType.META_INFORMATION) {
				metaFields.add(field);
			}
		}

		if (metaFields.isEmpty()) {
			return null;
		} else if (metaFields.size() > 1) {
			throw new MultipleJsonApiMetaInformationException(resourceClass.getCanonicalName());
		}
		return metaFields.get(0);
	}

	private static <T> ResourceField getLinksField(Class<T> resourceClass, Collection<ResourceField> classFields) {
		List<ResourceField> linksFields = new ArrayList<>(1);
		for (ResourceField field : classFields) {
			if (field.getResourceFieldType() == ResourceFieldType.LINKS_INFORMATION) {
				linksFields.add(field);
			}
		}

		if (linksFields.isEmpty()) {
			return null;
		} else if (linksFields.size() > 1) {
			throw new MultipleJsonApiLinksInformationException(resourceClass.getCanonicalName());
		}
		return linksFields.get(0);
	}

	public String getResourceType() {
		return resourceType;
	}

	public ResourceInstanceBuilder<?> getInstanceBuilder() {
		return instanceBuilder;
	}

	public Class<?> getResourceClass() {
		return resourceClass;
	}

	public ResourceField getIdField() {
		return idField;
	}

	public ResourceAttributesBridge getAttributeFields() {
		return attributeFields;
	}

	public List<ResourceField> getRelationshipFields() {
		return relationshipFields;
	}

	public ResourceField findRelationshipFieldByName(String name) {
		return getJsonField(name, relationshipFields);
	}

	public ResourceField findAttributeFieldByName(String name) {
		return getJsonField(name, attributeFields.getFields());
	}

	private static ResourceField getJsonField(String name, List<ResourceField> fields) {
		ResourceField foundField = null;
		for (ResourceField field : fields) {
			if (field.getJsonName().equals(name)) {
				foundField = field;
				break;
			}
		}
		return foundField;
	}

	public ResourceField getMetaField() {
		return metaField;
	}

	public ResourceField getLinksField() {
		return linksField;
	}

	/**
	 * Returns a set of field names which are not basic fields (resource
	 * attributes)
	 *
	 * @return not basic attribute names
	 */
	public Set<String> getNotAttributeFields() {
		Set<String> notAttributeFields = new HashSet<>();
		for (ResourceField relationshipField : relationshipFields) {
			notAttributeFields.add(relationshipField.getJsonName());
		}
		notAttributeFields.add(idField.getJsonName());
		if (metaField != null) {
			notAttributeFields.add(metaField.getUnderlyingName());
		}
		if (linksField != null) {
			notAttributeFields.add(linksField.getUnderlyingName());
		}
		return notAttributeFields;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ResourceInformation that = (ResourceInformation) o;
		return Objects.equals(resourceClass, that.resourceClass) && Objects.equals(resourceType, that.resourceType) && Objects.equals(idField, that.idField) && Objects.equals(attributeFields, that.attributeFields)
				&& Objects.equals(relationshipFields, that.relationshipFields) && Objects.equals(metaField, that.metaField) && Objects.equals(linksField, that.linksField);
	}

	@Override
	public int hashCode() {
		return Objects.hash(resourceClass, resourceType, idField, attributeFields, relationshipFields, metaField, linksField);
	}

	/**
	 * Converts the given id to a string.
	 *
	 * @param id
	 *            id
	 * @return stringified id
	 */
	public String toIdString(Object id) {
		if (id == null)
			return null;
		return id.toString();
	}

	/**
	 * Converts the given id string into its object representation.
	 *
	 * @param id
	 *            stringified id
	 * @return id
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Serializable parseIdString(String id) {
		Class idType = getIdField().getType();
		return parser.parse(id, idType);
	}

	/**
	 * @param resource
	 * @return id of the resource
	 */
	public Object getId(Object resource) {
		return PropertyUtils.getProperty(resource, idField.getUnderlyingName());
	}

	public void setId(Object resource, Object id) {
		PropertyUtils.setProperty(resource, idField.getUnderlyingName(), id);
	}

	@Deprecated // Temporary method until proper versioning/locking/timestamping
				// is implemented, used by JPA module
	public void verify(Object resource, Document requestDocument) {
	}

}