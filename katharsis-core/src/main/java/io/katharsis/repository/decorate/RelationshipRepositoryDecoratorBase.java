package io.katharsis.repository.decorate;

import java.io.Serializable;

import io.katharsis.queryspec.QuerySpec;
import io.katharsis.repository.RelationshipRepositoryV2;
import io.katharsis.resource.list.ResourceList;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ResourceRegistryAware;

public abstract class RelationshipRepositoryDecoratorBase<T, I extends Serializable, D, J extends Serializable>
		implements RelationshipRepositoryDecorator<T, I, D, J>, ResourceRegistryAware {

	private RelationshipRepositoryV2<T, I, D, J> decoratedObject;

	@Override
	public Class<T> getSourceResourceClass() {
		return decoratedObject.getSourceResourceClass();
	}

	@Override
	public Class<D> getTargetResourceClass() {
		return decoratedObject.getTargetResourceClass();
	}

	@Override
	public void setRelation(T source, J targetId, String fieldName) {
		decoratedObject.setRelation(source, targetId, fieldName);
	}

	@Override
	public void setRelations(T source, Iterable<J> targetIds, String fieldName) {
		decoratedObject.setRelations(source, targetIds, fieldName);
	}

	@Override
	public void addRelations(T source, Iterable<J> targetIds, String fieldName) {
		decoratedObject.addRelations(source, targetIds, fieldName);
	}

	@Override
	public void removeRelations(T source, Iterable<J> targetIds, String fieldName) {
		decoratedObject.removeRelations(source, targetIds, fieldName);
	}

	@Override
	public D findOneTarget(I sourceId, String fieldName, QuerySpec querySpec) {
		return decoratedObject.findOneTarget(sourceId, fieldName, querySpec);
	}

	@Override
	public ResourceList<D> findManyTargets(I sourceId, String fieldName, QuerySpec querySpec) {
		return decoratedObject.findManyTargets(sourceId, fieldName, querySpec);
	}

	@Override
	public void setDecoratedObject(RelationshipRepositoryV2<T, I, D, J> decoratedObject) {
		this.decoratedObject = decoratedObject;
	}

	@Override
	public void setResourceRegistry(ResourceRegistry resourceRegistry) {
		if (decoratedObject instanceof ResourceRegistryAware) {
			((ResourceRegistryAware) decoratedObject).setResourceRegistry(resourceRegistry);
		}
	}
}
