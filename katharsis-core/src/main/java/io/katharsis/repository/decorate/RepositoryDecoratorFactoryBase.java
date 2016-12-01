package io.katharsis.repository.decorate;

import java.io.Serializable;

import io.katharsis.repository.RelationshipRepositoryV2;
import io.katharsis.repository.ResourceRepositoryV2;

/**
 * Base class for {@links RepositoryDecorator} implementations doing nothing.
 */
public abstract class RepositoryDecoratorFactoryBase implements RepositoryDecoratorFactory {

	@Override
	public <T, I extends Serializable> ResourceRepositoryDecorator<T, I> decorateRepository(
			ResourceRepositoryV2<T, I> repository) {
		// nothing to decorate
		return null;
	}

	@Override
	public <T, I extends Serializable, D, J extends Serializable> RelationshipRepositoryDecorator<T, I, D, J> decorateRepository(
			RelationshipRepositoryV2<T, I, D, J> repository) {
		// nothing to decorate
		return null;
	}
}
