package io.katharsis.meta.internal;

import java.util.Collection;

import io.katharsis.meta.MetaLookup;
import io.katharsis.meta.model.MetaElement;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.repository.RelationshipRepositoryV2;
import io.katharsis.resource.exception.ResourceNotFoundException;
import io.katharsis.resource.list.ResourceList;
import io.katharsis.utils.PropertyUtils;

public class MetaRelationshipRepository implements RelationshipRepositoryV2<MetaElement, String, MetaElement, String> {

	private MetaLookup lookup;
	
	private Class<? extends MetaElement> sourceResourceClass;
	private Class<? extends MetaElement> targetResourceClass;

	public MetaRelationshipRepository(MetaLookup lookup, Class<? extends MetaElement> sourceClass,
			Class<? extends MetaElement> targetClass) {
		this.lookup = lookup;
		this.sourceResourceClass = sourceClass;
		this.targetResourceClass = targetClass;
	}

	@Override
	public MetaElement findOneTarget(String sourceId, String fieldName, QuerySpec querySpec) {
		MetaElement source = lookup.getMetaById().get(sourceId);
		if (source == null) {
			throw new ResourceNotFoundException(sourceId);
		}
		Object value = PropertyUtils.getProperty(source, fieldName);
		if (!(value instanceof MetaElement) && value != null) {
			throw new IllegalStateException("relation " + fieldName + " is not of type MetaElement, got " + value);
		}
		return (MetaElement) value;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResourceList<MetaElement> findManyTargets(String sourceId, String fieldName, QuerySpec querySpec) {
		MetaElement source = lookup.getMetaById().get(sourceId);
		if (source == null) {
			throw new ResourceNotFoundException(sourceId);
		}
		Object value = PropertyUtils.getProperty(source, fieldName);
		if (!(value instanceof Collection)) {
			throw new IllegalStateException("relation " + fieldName + " is not a collection, got " + value);
		}
		return querySpec.apply((Collection<MetaElement>) value);
	}

	@Override
	public Class<MetaElement> getSourceResourceClass() {
		return (Class<MetaElement>) sourceResourceClass;
	}

	@Override
	public Class<MetaElement> getTargetResourceClass() {
		return (Class<MetaElement>) targetResourceClass;
	}

	@Override
	public void setRelation(MetaElement source, String targetId, String fieldName) {
		throw new UnsupportedOperationException("repository is read-only");
	}

	@Override
	public void setRelations(MetaElement source, Iterable<String> targetIds, String fieldName) {
		throw new UnsupportedOperationException("repository is read-only");
	}

	@Override
	public void addRelations(MetaElement source, Iterable<String> targetIds, String fieldName) {
		throw new UnsupportedOperationException("repository is read-only");
	}

	@Override
	public void removeRelations(MetaElement source, Iterable<String> targetIds, String fieldName) {
		throw new UnsupportedOperationException("repository is read-only");
	}

	//	@SuppressWarnings("unchecked")
	//	public T findOne(String id, QuerySpec querySpec) {
	//		MetaElement metaElement = lookup.getMetaById().get(id);
	//		Class<T> resourceClass = this.getResourceClass();
	//		if (metaElement != null && resourceClass.isInstance(metaElement)) {
	//			return (T) metaElement;
	//		}
	//		throw new ResourceNotFoundException(id);
	//	}
	//
	//	@Override
	//	public ResourceList<T> findAll(QuerySpec querySpec) {
	//		Collection<T> values = filterByType(lookup.getMetaById().values());
	//		return querySpec.apply(values);
	//	}
	//
	//	@SuppressWarnings("unchecked")
	//	private Collection<T> filterByType(Collection<MetaElement> values) {
	//		Collection<T> results = new ArrayList<>();
	//		Class<T> resourceClass = this.getResourceClass();
	//		for (MetaElement element : values) {
	//			if (resourceClass.isInstance(element)) {
	//				results.add((T) element);
	//			}
	//		}
	//		return results;
	//	}
}