package io.katharsis.repository.information;

import io.katharsis.resource.information.ResourceInformation;

/**
 * Holds information about the type of a repository.
 */
public interface RepositoryInformation {

	Class<?> getRepositoryClass();

	/**
	 * @return information about the resources hold in this repository
	 */
	ResourceInformation getResourceInformation();
}
