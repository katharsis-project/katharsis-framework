package io.katharsis.repository.decorate;

import java.io.Serializable;

import io.katharsis.repository.RelationshipRepositoryV2;
import io.katharsis.repository.ResourceRepositoryV2;

/**
 * Allows to intercept calls to repositories by modules and make changes.
 */
public interface RepositoryDecoratorFactory {

	/**
	 * Allows to wrap a repository with {@link ResourceRepositoryDecorator}.
	 * 
	 * @param repository to wrap
	 * @return decorated repository
	 */
	public <T, I extends Serializable> ResourceRepositoryDecorator<T, I> decorateRepository(
			ResourceRepositoryV2<T, I> repository);

	/**
	 * Allows to wrap a repository with {@link RelationshipRepositoryDecorator}.
	 * 
	 * @param repository to wrap
	 * @return decorated repository
	 */
	public <T, I extends Serializable, D, J extends Serializable> RelationshipRepositoryDecorator<T, I, D, J> decorateRepository(
			RelationshipRepositoryV2<T, I, D, J> repository);

}
