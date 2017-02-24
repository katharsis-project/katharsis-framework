package io.katharsis.core.internal.resource;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

import io.katharsis.core.internal.utils.PreconditionUtil;
import io.katharsis.resource.annotations.LookupIncludeBehavior;
import io.katharsis.resource.information.ResourceField;
import io.katharsis.resource.information.ResourceFieldType;
import io.katharsis.resource.information.ResourceInformation;

public class ResourceFieldImpl implements ResourceField {

	private final String jsonName;

	private final String underlyingName;

	private final Class<?> type;

	private String oppositeResourceType;

	private final Type genericType;

	private final boolean lazy;

	private LookupIncludeBehavior lookupIncludeBehavior;

	private boolean includeByDefault;

	private ResourceFieldType resourceFieldType;

	private String oppositeName;

	private ResourceInformation parentResourceInformation;

	public ResourceFieldImpl(String jsonName, String underlyingName, ResourceFieldType resourceFieldType, Class<?> type, Type genericType, String oppositeResourceType) {
		this(jsonName, underlyingName, resourceFieldType, type, genericType, oppositeResourceType, null, true, false, LookupIncludeBehavior.NONE);
	}

	public ResourceFieldImpl(String jsonName, String underlyingName, ResourceFieldType resourceFieldType, Class<?> type, Type genericType, String oppositeResourceType, String oppositeName, boolean lazy,
			boolean includeByDefault, LookupIncludeBehavior lookupIncludeBehavior) {
		this.jsonName = jsonName;
		this.underlyingName = underlyingName;
		this.resourceFieldType = resourceFieldType;
		this.includeByDefault = includeByDefault;
		this.type = type;
		this.genericType = genericType;
		this.lazy = lazy;
		this.lookupIncludeBehavior = lookupIncludeBehavior;
		this.oppositeName = oppositeName;
		this.oppositeResourceType = oppositeResourceType;
	}

	public ResourceFieldType getResourceFieldType() {
		return resourceFieldType;
	}

	/**
	 * See also
	 * {@link io.katharsis.resource.annotations.JsonApiLookupIncludeAutomatically}
	 * }
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

	public String getOppositeResourceType() {
		PreconditionUtil.assertEquals("not an association", ResourceFieldType.RELATIONSHIP, resourceFieldType);
		PreconditionUtil.assertNotNull("resourceType must not be null", oppositeResourceType);
		return oppositeResourceType;
	}

	public Class<?> getType() {
		return type;
	}

	public Type getGenericType() {
		return genericType;
	}

	/**
	 * Returns a flag which indicate if a field should not be serialized
	 * automatically.
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
		ResourceFieldImpl that = (ResourceFieldImpl) o;
		return Objects.equals(jsonName, that.jsonName) && Objects.equals(underlyingName, that.underlyingName) && Objects.equals(type, that.type) && Objects.equals(lookupIncludeBehavior, that.lookupIncludeBehavior)
				&& Objects.equals(includeByDefault, that.includeByDefault) && Objects.equals(genericType, that.genericType) && Objects.equals(lazy, that.lazy);
	}

	@Override
	public int hashCode() {
		return Objects.hash(jsonName, underlyingName, type, genericType, lazy, includeByDefault, lookupIncludeBehavior);
	}

	/**
	 * Returns the non-collection type. Matches {@link #getType()} for
	 * non-collections. Returns the type argument in case of a collection type.
	 *
	 * @return Ask Remmo
	 */
	public Class<?> getElementType() {
		if (Iterable.class.isAssignableFrom(type)) {
			return (Class<?>) ((ParameterizedType) getGenericType()).getActualTypeArguments()[0];
		} else {
			return type;
		}
	}

	public ResourceInformation getParentResourceInformation() {
		return parentResourceInformation;
	}

	public void setResourceInformation(ResourceInformation resourceInformation) {
		this.parentResourceInformation = resourceInformation;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[jsonName=" + jsonName + ",resourceType=" + parentResourceInformation.getResourceType() + "]";
	}

	public boolean isCollection() {
		return Iterable.class.isAssignableFrom(getType());
	}
}