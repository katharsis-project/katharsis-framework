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

	/**
	 * @return resourceType of the opposite resource in case of a relation.
	 */
	public String getOppositeResourceType();

	/**
	 * @return name used in Json
	 */
	public String getJsonName();

	/**
	 * @return name used in Java
	 */
	public String getUnderlyingName();

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
	 * @return the non-collection type. Matches {@link #getType()} for
	 *         non-collections. Returns the type argument in case of a
	 *         collection type.
	 */
	public Class<?> getElementType();

	/**
	 * @return resourceInformation this field belongs to.
	 */
	public ResourceInformation getParentResourceInformation();

	public void setResourceInformation(ResourceInformation resourceInformation);

	public boolean isCollection();

	/**
	 * @return access information for this resource (postable, patchable)
	 */
	ResourceFieldAccess getAccess();

}