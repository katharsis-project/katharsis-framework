package io.katharsis.resource.field;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.katharsis.resource.information.ResourceInformation;

public class ResourceField {

	public enum LookupIncludeBehavior {
		NONE,
		AUTOMATICALLY_WHEN_NULL,
		AUTOMATICALLY_ALWAYS,
	}

	public enum ResourceFieldType {
		ID,
		ATTRIBUTE,
		RELATIONSHIP,
		META_INFORMATION,
		LINKS_INFORMATION;

		public static ResourceFieldType get(boolean id, boolean linksInfo, boolean metaInfo, boolean association) {
			if (id) {
				return ResourceFieldType.ID;
			}
			if (association) {
				return ResourceFieldType.RELATIONSHIP;
			}
			if (linksInfo) {
				return ResourceFieldType.LINKS_INFORMATION;
			}
			if (metaInfo) {
				return ResourceFieldType.META_INFORMATION;
			}
			return ResourceFieldType.ATTRIBUTE;
		}

		public List<ResourceField> filter(List<ResourceField> fields) {
			ArrayList<ResourceField> results = new ArrayList<>();
			for (ResourceField field : fields) {
				if (field.getResourceFieldType() == this) {
					results.add(field);
				}
			}
			return results;
		}
	}

	private final String jsonName;

	private final String underlyingName;

	private final Class<?> type;

	private final Type genericType;

	private final boolean lazy;

	private LookupIncludeBehavior lookupIncludeBehavior;

	private boolean includeByDefault;

	private ResourceFieldType resourceFieldType;

	private String oppositeName;

	private ResourceInformation resourceInformation;

	public ResourceField(String jsonName, String underlyingName, ResourceFieldType resourceFieldType, Class<?> type,
			Type genericType) {
		this(jsonName, underlyingName, resourceFieldType, type, genericType, null, true, false, LookupIncludeBehavior.NONE);
	}

	public ResourceField(String jsonName, String underlyingName, ResourceFieldType resourceFieldType, Class<?> type,
			Type genericType, String oppositeName, boolean lazy, boolean includeByDefault,
			LookupIncludeBehavior lookupIncludeBehavior) {
		this.jsonName = jsonName;
		this.underlyingName = underlyingName;
		this.resourceFieldType = resourceFieldType;
		this.includeByDefault = includeByDefault;
		this.type = type;
		this.genericType = genericType;
		this.lazy = lazy;
		this.lookupIncludeBehavior = lookupIncludeBehavior;
		this.oppositeName = oppositeName;
	}

	public ResourceFieldType getResourceFieldType() {
		return resourceFieldType;
	}

	/**
	 * See also {@link io.katharsis.resource.annotations.JsonApiLookupIncludeAutomatically}}
	 *
	 * @return if lookup should be performed
	 */
	public LookupIncludeBehavior getLookupIncludeAutomatically() {
		return lookupIncludeBehavior;
	}

	/**
	 * @return name of opposite attribute in case of a bidirectional relation.
	 */
	public String getOppositeName() {
		return oppositeName;
	}

	public String getJsonName() {
		return jsonName;
	}

	public String getUnderlyingName() {
		return underlyingName;
	}

	public Class<?> getType() {
		return type;
	}

	public Type getGenericType() {
		return genericType;
	}

	/**
	 * Returns a flag which indicate if a field should not be serialized automatically.
	 * 
	 * @return true if a field is lazy
	 */
	public boolean isLazy() {
		return lazy;
	}

	public boolean getIncludeByDefault() {
		return includeByDefault;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ResourceField that = (ResourceField) o;
		return Objects.equals(jsonName, that.jsonName) && Objects.equals(underlyingName, that.underlyingName)
				&& Objects.equals(type, that.type) && Objects.equals(lookupIncludeBehavior, that.lookupIncludeBehavior)
				&& Objects.equals(includeByDefault, that.includeByDefault) && Objects.equals(genericType, that.genericType)
				&& Objects.equals(lazy, that.lazy);
	}

	@Override
	public int hashCode() {
		return Objects.hash(jsonName, underlyingName, type, genericType, lazy, includeByDefault, lookupIncludeBehavior);
	}

	/**
	 * Returns the non-collection type. Matches {@link #getType()} for non-collections. Returns the type argument in case of 
	 * a collection type.
	 *
	 * @return Ask Remmo
	 */
	public Class<?> getElementType() {
		if (Iterable.class.isAssignableFrom(type)) {
			return (Class<?>) ((ParameterizedType) getGenericType()).getActualTypeArguments()[0];
		}
		else {
			return type;
		}
	}

	public ResourceInformation getResourceInformation(){
		return resourceInformation;
	}
	
	public void setResourceInformation(ResourceInformation resourceInformation) {
		this.resourceInformation = resourceInformation;
	}
	
	@Override
	public String toString(){
		return getClass().getSimpleName() + "[jsonName=" + jsonName + ",resourceType=" + resourceInformation.getResourceType() + "]";
	}

	public boolean isCollection() {
		return Iterable.class.isAssignableFrom(getType());
	}
}