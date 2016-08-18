package io.katharsis.resource.information;

/**
 * A builder which creates ResourceInformation instances of a specific class.
 */
public interface ResourceInformationBuilder {
	
	/**
	 * @param resourceClass
	 * @return true if this builder can process the provided resource class
	 */
	public boolean accept(Class<?> resourceClass);

	/**
	 * @param resourceClass
	 * @return ResourceInformation for the provided resource class.
	 */
    public ResourceInformation build(Class<?> resourceClass);

}
