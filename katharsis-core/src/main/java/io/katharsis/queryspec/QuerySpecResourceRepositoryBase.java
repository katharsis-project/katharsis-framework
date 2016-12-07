package io.katharsis.queryspec;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;

import io.katharsis.resource.exception.ResourceNotFoundException;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ResourceRegistryAware;
import io.katharsis.utils.PreconditionUtil;

/**
 * Base implements for {@link QuerySpecResourceRepository} implementing most of the methods.
 * Unless {@link #save(Object)} and  {@link #delete(Object)} get overridden, this repository
 * is read-only. Only {@link #findAll(QuerySpec)} needs to be implemented to have a working repository.
 *
 * @param <T> resource type
 * @param <I> identity type
 * 
 * @deprecated use ResourceRepositoryBase instead
 */
public abstract class QuerySpecResourceRepositoryBase<T, I extends Serializable>
		implements QuerySpecResourceRepository<T, I>, ResourceRegistryAware {

	private Class<T> resourceClass;

	private ResourceRegistry resourceRegistry;

	protected QuerySpecResourceRepositoryBase(Class<T> resourceClass) {
		this.resourceClass = resourceClass;
	}

	@Override
	public Class<T> getResourceClass() {
		return resourceClass;
	}

	/**
	 * Forwards to {@link #findAll(QuerySpec)}
	 * 
	 * @param id of the resource
	 * @param querySpec for field and relation inclusion
	 * @return resource
	 */
	@Override
	public T findOne(I id, QuerySpec querySpec) {
		RegistryEntry<T> entry = resourceRegistry.getEntry(resourceClass);
		String idName = entry.getResourceInformation().getIdField().getUnderlyingName();

		QuerySpec idQuerySpec = querySpec.duplicate();
		idQuerySpec.addFilter(new FilterSpec(Arrays.asList(idName), FilterOperator.EQ, id));
		Iterable<T> iterable = findAll(idQuerySpec);
		Iterator<T> iterator = iterable.iterator();
		if (iterator.hasNext()) {
			T resource = iterator.next();
			PreconditionUtil.assertFalse("expected unique result", iterator.hasNext());
			return resource;
		}
		else {
			throw new ResourceNotFoundException("resource not found");
		}
	}

	/**
	 * Forwards to {@link #findAll(QuerySpec)}
	 * 
	 * @param ids of the resources
	 * @param querySpec for field and relation inclusion
	 * @return resources
	 */
	@Override
	public Iterable<T> findAll(Iterable<I> ids, QuerySpec querySpec) {
		RegistryEntry<T> entry = resourceRegistry.getEntry(resourceClass);
		String idName = entry.getResourceInformation().getIdField().getUnderlyingName();

		QuerySpec idQuerySpec = querySpec.duplicate();
		idQuerySpec.addFilter(new FilterSpec(Arrays.asList(idName), FilterOperator.EQ, ids));
		return findAll(idQuerySpec);
	}

	/**
	 * read-only by default
	 * 
	 * @param resource to save
	 * @return saved resource
	 */
	@Override
	public <S extends T> S save(S resource) {
		throw new UnsupportedOperationException();
	}

	/**
	 * read-only by default
	 * 
	 * @param id of resource to delete
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
