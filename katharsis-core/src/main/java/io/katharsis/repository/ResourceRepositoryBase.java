package io.katharsis.repository;

import java.io.Serializable;

import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.QuerySpecResourceRepositoryBase;
import io.katharsis.resource.list.ResourceList;
import io.katharsis.resource.registry.ResourceRegistry;

/**
 * Recommended base class to implement a resource repository making use of the QuerySpec and ResourceList.
 * Note that the former  {@link QuerySpecResourceRepositoryBase} will be removed in the near future.
 * 
 * Base implements for {@link ResourceRepositoryV2} implementing most of the methods.
 * Unless {@link #save(Object)} and  {@link #delete(Object)} get overridden, this repository
 * is read-only. Only {@link #findAll(QuerySpec)} needs to be implemented to have a working repository.
 *
 * @param <T> resource type
 * @param <I> identity type
 */
public abstract class ResourceRepositoryBase<T, I extends Serializable> extends QuerySpecResourceRepositoryBase<T, I>
		implements ResourceRepositoryV2<T, I> {

	public ResourceRepositoryBase(Class<T> resourceClass) {
		super(resourceClass);
	}

	@Override
	public Class<T> getResourceClass() {// NOSONAR ok the override since not deprecated
		return super.getResourceClass();
	}

	/**
	 * Forwards to {@link #findAll(QuerySpec)}
	 * 
	 * @param id of the resource
	 * @param querySpec for field and relation inclusion
	 * @return resource
	 */
	@Override
	public T findOne(I id, QuerySpec querySpec) {// NOSONAR
		return super.findOne(id, querySpec);
	}

	/**
	 * Forwards to {@link #findAll(QuerySpec)}
	 * 
	 * @param ids of the resources
	 * @param querySpec for field and relation inclusion
	 * @return resources
	 */
	@Override
	public ResourceList<T> findAll(Iterable<I> ids, QuerySpec querySpec) {// NOSONAR
		return (ResourceList<T>) super.findAll(ids, querySpec);
	}

	/**
	 * read-only by default
	 * 
	 * @param resource to save
	 * @return saved resource
	 */
	@Override
	public <S extends T> S save(S resource) {// NOSONAR
		return super.save(resource);
	}

	/**
	 * invokates save by default
	 * 
	 * @param resource to create
	 * @return created resource
	 */
	@Override
	public <S extends T> S create(S resource) {
		return save(resource);
	}

	/**
	 * read-only by default
	 * 
	 * @param id of resource to delete
	 */
	@Override
	public void delete(I id) {// NOSONAR
		super.delete(id);
	}

	@Override
	public void setResourceRegistry(ResourceRegistry resourceRegistry) { // NOSONAR
		super.setResourceRegistry(resourceRegistry);
	}
}
