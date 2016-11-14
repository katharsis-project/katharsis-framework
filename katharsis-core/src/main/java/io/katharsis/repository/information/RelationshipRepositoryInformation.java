package io.katharsis.repository.information;

import io.katharsis.resource.information.ResourceInformation;

/**
 * Holds information about the type of a resource repository.
 */
public interface RelationshipRepositoryInformation extends RepositoryInformation {

	/**
	 * @return information about the source of the relationship.
	 */
	ResourceInformation getSourceResourceInformation();

}
