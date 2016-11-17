package io.katharsis.repository;

import java.io.Serializable;

import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.QuerySpecResourceRepository;
import io.katharsis.queryspec.QuerySpecResourceRepositoryBase;
import io.katharsis.resource.list.ResourceList;

/**
 * Recommended base class to implement a resource repository making use of the QuerySpec and ResourceList.
 * Note that the former  {@link QuerySpecResourceRepositoryBase} will be removed in the near future.
 * 
 * Base implements for {@link QuerySpecResourceRepository} implementing most of the methods.
 * Unless {@link #save(Object)} and  {@link #delete(Object)} get overridden, this repository
 * is read-only. Only {@link #findAll(QuerySpec)} needs to be implemented to have a working repository.
 *
 * @param <T> resource type
 * @param <I> identity type
 */
public abstract class ResourceRepositoryBase<T, I extends Serializable> extends QuerySpecResourceRepositoryBase<T, I> {

	protected ResourceRepositoryBase(Class<T> resourceClass) {
		super(resourceClass);
	}

	@Override
	public ResourceList<T> findAll(Iterable<I> ids, QuerySpec querySpec) {
		return (ResourceList<T>) super.findAll(ids, querySpec);
	}

	@Override
	public abstract ResourceList<T> findAll(QuerySpec querySpec);
}
