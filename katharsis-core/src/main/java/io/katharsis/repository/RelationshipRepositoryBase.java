package io.katharsis.repository;

import java.io.Serializable;

import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.QuerySpecRelationshipRepository;
import io.katharsis.queryspec.QuerySpecRelationshipRepositoryBase;
import io.katharsis.resource.annotations.JsonApiToMany;
import io.katharsis.resource.annotations.JsonApiToOne;
import io.katharsis.resource.list.ResourceList;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.utils.MultivaluedMap;

/**
 * Recommended base class to implement a relationship repository making use of the QuerySpec and ResourceList.
 * Note that the former  {@link QuerySpecRelationshipRepositoryBase} will be removed in the near future.
 *  
 * Base implementation for {@link RelationshipRepositoryV2} implementing <b>ALL</b> of the methods.
 * Makes use of the source and target resource repository to implement the relationship features.
 * Modification are implemented by fetching the target resources, adding them the the source
 * resource with reflection and then saving the source resource. Lookup is implemented by
 * querying the target resource repository and filtering in the opposite relationship direction.
 * Not that {@link JsonApiToMany} resp. {@link JsonApiToOne} need to declare the opposite name
 * for the relations.
 * 
 * Warning: this implementation does not take care of bidirectionality. This is usuefally very implementation-specific
 * and cannot be handled in a generic fashion. Setting a relation on a resource and 
 * saving it assumes that the save operation makes sure that the relationship is setup in a bi-directional way.
 * You may run here into issues, for example in basic in-memory repositories (implement getters, setters, adds and remove on
 * beans accordingly) or with JPA (where only the owning entity can update a relation).
 *
 * @param <T> source resource type
 * @param <I> source identity type
 * @param <D> target resource type
 * @param <J> target identity type
 */
public class RelationshipRepositoryBase<T, I extends Serializable, D, J extends Serializable>
		extends QuerySpecRelationshipRepositoryBase<T, I, D, J> implements RelationshipRepositoryV2<T, I, D, J> {

	public RelationshipRepositoryBase(Class<T> sourceResourceClass, Class<D> targetResourceClass) {
		super(sourceResourceClass, targetResourceClass);
	}

	@Override
	public ResourceList<D> findManyTargets(I sourceId, String fieldName, QuerySpec querySpec) {
		return (ResourceList<D>) super.findManyTargets(sourceId, fieldName, querySpec);
	}

	@Override
	public D findOneTarget(I sourceId, String fieldName, QuerySpec querySpec) { // NOSONAR ok to override since not deprecated
		return super.findOneTarget(sourceId, fieldName, querySpec);
	}

	@Override
	public void setRelation(T source, J targetId, String fieldName) { // NOSONAR ok to override since not deprecated
		super.setRelation(source, targetId, fieldName);
	}

	@Override
	public void setRelations(T source, Iterable<J> targetIds, String fieldName) { // NOSONAR ok to override since not deprecated
		super.setRelations(source, targetIds, fieldName);
	}

	@Override
	public void addRelations(T source, Iterable<J> targetIds, String fieldName) { // NOSONAR ok to override since not deprecated
		super.addRelations(source, targetIds, fieldName);
	}

	@Override
	public void removeRelations(T source, Iterable<J> targetIds, String fieldName) { // NOSONAR ok to override since not deprecated
		super.removeRelations(source, targetIds, fieldName);
	}

	@SuppressWarnings("unchecked")
	@Override
	public MultivaluedMap<I, D> findTargets(Iterable<I> sourceIds, String fieldName, QuerySpec querySpec) { // NOSONAR ok to override since not deprecated
		return super.findTargets(sourceIds, fieldName, querySpec);
	}

	@Override
	public Class<T> getSourceResourceClass() { // NOSONAR ok to override since not deprecated
		return super.getSourceResourceClass();
	}

	@Override
	public Class<D> getTargetResourceClass() { // NOSONAR ok to override since not deprecated
		return super.getTargetResourceClass();
	}

	@Override
	public void setResourceRegistry(ResourceRegistry resourceRegistry) { // NOSONAR ok to override since not deprecated
		super.setResourceRegistry(resourceRegistry);
	}
}
