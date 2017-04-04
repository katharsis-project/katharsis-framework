package io.katharsis.resource.information;

import java.lang.reflect.Type;

import io.katharsis.resource.annotations.LookupIncludeBehavior;

public interface ResourceField {

	public ResourceFieldType getResourceFieldType();

	/**
	 * See also
	 * {@link io.katharsis.resource.annotations.JsonApiLookupIncludeAutomatically}
	 * }
	 *
	 * @return if lookup should be performed
	 */
	public LookupIncludeBehavior getLookupIncludeAutomatically();

	/**
	 * @return name of opposite attribute in case of a bidirectional relation.
	 */
	public String getOppositeName();

	public String getJsonName();

	public String getUnderlyingName();

	public String getOppositeResourceType();

	public Class<?> getType();

	public Type getGenericType();

	/**
	 * Returns a flag which indicate if a field should not be serialized
	 * automatically.
	 * 
	 * @return true if a field is lazy
	 */
	public boolean isLazy();

	public boolean getIncludeByDefault();

	/**
	 * Returns the non-collection type. Matches {@link #getType()} for
	 * non-collections. Returns the type argument in case of a collection type.
	 *
	 * @return Ask Remmo
	 */
	public Class<?> getElementType();

	public ResourceInformation getParentResourceInformation();

	public void setResourceInformation(ResourceInformation resourceInformation);

	public boolean isCollection();
}