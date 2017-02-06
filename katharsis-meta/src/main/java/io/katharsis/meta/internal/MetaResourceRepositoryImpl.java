package io.katharsis.meta.internal;

import java.util.ArrayList;
import java.util.Collection;

import io.katharsis.errorhandling.exception.ResourceNotFoundException;
import io.katharsis.meta.MetaLookup;
import io.katharsis.meta.model.MetaElement;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.repository.ResourceRepositoryBase;
import io.katharsis.resource.list.ResourceList;

public class MetaResourceRepositoryImpl<T> extends ResourceRepositoryBase<T, String> {

	private MetaLookup lookup;

	public MetaResourceRepositoryImpl(MetaLookup lookup, Class<T> resourceClass) {
		super(resourceClass);
		this.lookup = lookup;
	}

	@SuppressWarnings("unchecked")
	public T findOne(String id, QuerySpec querySpec) {
		MetaElement metaElement = lookup.getMetaById().get(id);
		Class<T> resourceClass = this.getResourceClass();
		if (metaElement != null && resourceClass.isInstance(metaElement)) {
			return (T) metaElement;
		}
		throw new ResourceNotFoundException(id);
	}

	@Override
	public ResourceList<T> findAll(QuerySpec querySpec) {
		Collection<T> values = filterByType(lookup.getMetaById().values());
		return querySpec.apply(values);
	}

	@SuppressWarnings("unchecked")
	private Collection<T> filterByType(Collection<MetaElement> values) {
		Collection<T> results = new ArrayList<>();
		Class<T> resourceClass = this.getResourceClass();
		for (MetaElement element : values) {
			if (resourceClass.isInstance(element)) {
				results.add((T) element);
			}
		}
		return results;
	}
}