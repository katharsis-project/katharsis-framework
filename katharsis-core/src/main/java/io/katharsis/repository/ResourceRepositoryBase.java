package io.katharsis.repository;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;

import io.katharsis.core.internal.utils.PreconditionUtil;
import io.katharsis.errorhandling.exception.ResourceNotFoundException;
import io.katharsis.queryspec.FilterOperator;
import io.katharsis.queryspec.FilterSpec;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.resource.list.ResourceList;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ResourceRegistryAware;

/**
 * Recommended base class to implement a resource repository making use of the
 * QuerySpec and ResourceList. Note that the former
 * {@link ResourceRepositoryBase} will be removed in the near future.
 * 
 * Base implements for {@link ResourceRepositoryV2} implementing most of the
 * methods. Unless {@link #save(T)} and {@link #delete(I)} get
 * overridden, this repository is read-only. Only {@link #findAll(QuerySpec)}
 * needs to be implemented to have a working repository.
 *
 * @param <T>
 *            resource type
 * @param <I>
 *            identity type
 */
public abstract class ResourceRepositoryBase<T, I extends Serializable> implements ResourceRepositoryV2<T, I>, ResourceRegistryAware {

	private Class<T> resourceClass;

	private ResourceRegistry resourceRegistry;

	protected ResourceRepositoryBase(Class<T> resourceClass) {
		this.resourceClass = resourceClass;
	}

	@Override
	public Class<T> getResourceClass() {
		return resourceClass;
	}

	/**
	 * Forwards to {@link #findAll(QuerySpec)}
	 * 
	 * @param id
	 *            of the resource
	 * @param querySpec
	 *            for field and relation inclusion
	 * @return resource
	 */
	@Override
	public T findOne(I id, QuerySpec querySpec) {
		RegistryEntry entry = resourceRegistry.findEntry(resourceClass);
		String idName = entry.getResourceInformation().getIdField().getUnderlyingName();

		QuerySpec idQuerySpec = querySpec.duplicate();
		idQuerySpec.addFilter(new FilterSpec(Arrays.asList(idName), FilterOperator.EQ, id));
		Iterable<T> iterable = findAll(idQuerySpec);
		Iterator<T> iterator = iterable.iterator();
		if (iterator.hasNext()) {
			T resource = iterator.next();
			PreconditionUtil.assertFalse("expected unique result", iterator.hasNext());
			return resource;
		} else {
			throw new ResourceNotFoundException("resource not found");
		}
	}

	/**
	 * Forwards to {@link #findAll(QuerySpec)}
	 * 
	 * @param ids
	 *            of the resources
	 * @param querySpec
	 *            for field and relation inclusion
	 * @return resources
	 */
	@Override
	public ResourceList<T> findAll(Iterable<I> ids, QuerySpec querySpec) {
		RegistryEntry entry = resourceRegistry.findEntry(resourceClass);
		String idName = entry.getResourceInformation().getIdField().getUnderlyingName();

		QuerySpec idQuerySpec = querySpec.duplicate();
		idQuerySpec.addFilter(new FilterSpec(Arrays.asList(idName), FilterOperator.EQ, ids));
		return findAll(idQuerySpec);
	}

	/**
	 * read-only by default
	 * 
	 * @param resource
	 *            to save
	 * @return saved resource
	 */
	@Override
	public <S extends T> S save(S resource) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <S extends T> S create(S resource) {
		return save(resource);
	}

	/**
	 * read-only by default
	 * 
	 * @param id
	 *            of resource to delete
	 */
	@Override
	public void delete(I id) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setResourceRegistry(ResourceRegistry resourceRegistry) {
		this.resourceRegistry = resourceRegistry;
	}
}
