package io.katharsis.repository.information;

import java.util.Map;

import io.katharsis.resource.information.ResourceInformation;

/**
 * Holds information about the type of a resource repository.
 */
public interface ResourceRepositoryInformation extends RepositoryInformation {

	/**
	 * @return information about the resources hold in this repository
	 */
	ResourceInformation getResourceInformation();

	/**
	 * @return path from which the repository is accessible
	 */
	String getPath();
	

	Map<String, RepositoryAction> getActions();
}
