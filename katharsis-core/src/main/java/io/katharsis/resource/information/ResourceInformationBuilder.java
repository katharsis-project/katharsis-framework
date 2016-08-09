package io.katharsis.resource.information;

/**
 * A builder interface which creates ResourceInformation instances of a specific class.
 */
public interface ResourceInformationBuilder {
	
	public boolean accept(Class<?> resourceClass) ;
	
	public ResourceInformation build(Class<?> resourceClass) ;
	
}
