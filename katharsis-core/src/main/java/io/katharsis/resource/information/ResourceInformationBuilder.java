package io.katharsis.resource.information;

/**
 * A builder which creates ResourceInformation instances of a specific class.
 */
public interface ResourceInformationBuilder {

    public ResourceInformation build(Class<?> resourceClass);

}
