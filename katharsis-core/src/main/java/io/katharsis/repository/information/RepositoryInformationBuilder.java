package io.katharsis.repository.information;

/**
 * A builder which creates RepositoryInformation instances of a specific class.
 */
public interface RepositoryInformationBuilder {

	/**
	 * @param repositoryClass repository class
	 * @return true if this builder can process the provided repository class
	 */
	boolean accept(Object repository);

	/**
	 * @param repositoryClass resource class
	 * @return RepositoryInformation for the provided repository class.
	 */
	RepositoryInformation build(Object repository, RepositoryInformationBuilderContext context);

}
