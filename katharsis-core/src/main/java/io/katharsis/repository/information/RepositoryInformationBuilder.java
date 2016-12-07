package io.katharsis.repository.information;

/**
 * A builder which creates RepositoryInformation instances from repositories or their classes.
 * Building information from the actual object class is a bit more flexible as it allows
 * the reuse of the same class for multiple, different repositories classes. 
 */
public interface RepositoryInformationBuilder {

	/**
	 * @param repositoryClass class
	 * @return true if this builder can process the provided repository class
	 */
	boolean accept(Class<?> repositoryClass);

	/**
	 * @param repository repository ibhect
	 * @return true if this builder can process the provided repository class
	 */
	boolean accept(Object repository);

	/**
	 * @param repository object
	 * @return RepositoryInformation for the provided repository class.
	 */
	RepositoryInformation build(Object repository, RepositoryInformationBuilderContext context);

	/**
	 * @param repositoryClass repository class
	 * @return RepositoryInformation for the provided repository class.
	 */
	RepositoryInformation build(Class<?> repositoryClass, RepositoryInformationBuilderContext context);

}
