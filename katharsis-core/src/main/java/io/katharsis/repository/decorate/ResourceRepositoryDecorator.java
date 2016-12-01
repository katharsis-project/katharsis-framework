package io.katharsis.repository.decorate;

import java.io.Serializable;

import io.katharsis.repository.ResourceRepositoryV2;
import io.katharsis.utils.Decorator;

public interface ResourceRepositoryDecorator<T, I extends Serializable> extends ResourceRepositoryV2<T, I>, Decorator<ResourceRepositoryV2<T, I>> {

	@Override
	public void setDecoratedObject(ResourceRepositoryV2<T, I> decoratedObject);
}
