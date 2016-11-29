package io.katharsis.repository.decorate;

import java.io.Serializable;

import io.katharsis.repository.RelationshipRepositoryV2;

public interface RelationshipRepositoryDecorator<T, I extends Serializable, D, J extends Serializable>
		extends RelationshipRepositoryV2<T, I, D, J> {

	public void setDecoratedObject(RelationshipRepositoryV2<T, I, D, J> decoratedObject);
}
