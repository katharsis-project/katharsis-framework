package io.katharsis.queryspec;

import io.katharsis.utils.CompareUtils;

import java.util.*;
import java.util.Map.Entry;

public class QuerySpec {

	private Class<?> resourceClass;

	private PagingSpec pagingSpec;

	private List<FilterSpec> filters = new ArrayList<>();

	private List<SortSpec> sort = new ArrayList<>();

	private List<IncludeFieldSpec> includedFields = new ArrayList<>();

	private List<IncludeRelationSpec> includedRelations = new ArrayList<>();

	private Map<Class<?>, QuerySpec> relatedSpecs = new HashMap<>();

	public QuerySpec(Class<?> resourceClass) {
		this.resourceClass = resourceClass;
	}

	public QuerySpec(Class<?> resourceClass, PagingSpec pagingSpec) {
		this.resourceClass = resourceClass;
		this.pagingSpec = pagingSpec;
	}

	public Class<?> getResourceClass() {
		return resourceClass;
	}

	/**
	 * Evaluates this querySpec against the provided list in memory. It supports
	 * sorting, filter and paging.
	 * <p>
	 * TODO currently ignores relations and inclusions, has room for improvements
	 *
	 * @param <T>       the type of resources in this Iterable
	 * @param resources resources
	 * @return sorted, filtered list.
	 */
	public <T> List<T> apply(Iterable<T> resources) {
		InMemoryEvaluator eval = new InMemoryEvaluator();
		return eval.eval(resources, this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((filters == null) ? 0 : filters.hashCode());
		result = prime * result + ((includedFields == null) ? 0 : includedFields.hashCode());
		result = prime * result + ((includedRelations == null) ? 0 : includedRelations.hashCode());
		result = prime * result + ((pagingSpec == null) ? 0 : pagingSpec.hashCode());
		result = prime * result + ((relatedSpecs == null) ? 0 : relatedSpecs.hashCode());
		result = prime * result + ((sort == null) ? 0 : sort.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		QuerySpec other = (QuerySpec) obj;
		return CompareUtils.isEquals(filters, other.filters) // NOSONAR
				&& CompareUtils.isEquals(includedFields, other.includedFields)
				&& CompareUtils.isEquals(includedRelations, other.includedRelations)
				&& CompareUtils.isEquals(pagingSpec, other.pagingSpec) && CompareUtils.isEquals(relatedSpecs, other.relatedSpecs)
				&& CompareUtils.isEquals(sort, other.sort);
	}

	public PagingSpec getPagingSpec() {
		return pagingSpec;
	}

	public void setPagingSpec(PagingSpec pagingSpec) {
		this.pagingSpec = pagingSpec;
	}

	public List<FilterSpec> getFilters() {
		return filters;
	}

	public void setFilters(List<FilterSpec> filters) {
		this.filters = filters;
	}

	public List<SortSpec> getSort() {
		return sort;
	}

	public void setSort(List<SortSpec> sort) {
		this.sort = sort;
	}

	public List<IncludeFieldSpec> getIncludedFields() {
		return includedFields;
	}

	public void setIncludedFields(List<IncludeFieldSpec> includedFields) {
		this.includedFields = includedFields;
	}

	public List<IncludeRelationSpec> getIncludedRelations() {
		return includedRelations;
	}

	public void setIncludedRelations(List<IncludeRelationSpec> includedRelations) {
		this.includedRelations = includedRelations;
	}

	public Map<Class<?>, QuerySpec> getRelatedSpecs() {
		return relatedSpecs;
	}

	public void setRelatedSpecs(Map<Class<?>, QuerySpec> relatedSpecs) {
		this.relatedSpecs = relatedSpecs;
	}

	public void addFilter(FilterSpec filterSpec) {
		this.filters.add(filterSpec);
	}

	public void addSort(SortSpec sortSpec) {
		this.sort.add(sortSpec);
	}

	public void includeField(List<String> attrPath) {
		this.includedFields.add(new IncludeFieldSpec(attrPath));
	}

	public void includeRelation(List<String> attrPath) {
		this.includedRelations.add(new IncludeRelationSpec(attrPath));
	}

	/**
	 * @param resourceClass resource class
	 * @return QuerySpec for the given class, either the root QuerySpec or any
	 * related QuerySpec.
	 */
	public QuerySpec getQuerySpec(Class<?> resourceClass) {
		if (resourceClass.equals(this.resourceClass))
			return this;
		return relatedSpecs.get(resourceClass);
	}

	public QuerySpec getOrCreateQuerySpec(Class<?> resourceClass) {
		QuerySpec querySpec = getQuerySpec(resourceClass);
		if (querySpec == null) {
			querySpec = new QuerySpec(resourceClass);
			relatedSpecs.put(resourceClass, querySpec);
		}
		return querySpec;
	}

	public void putRelatedSpec(Class<?> relatedResourceClass, QuerySpec relatedSpec) {
		if (relatedResourceClass.equals(resourceClass)) {
			throw new IllegalArgumentException("cannot set related spec with root resourceClass");
		}
		relatedSpecs.put(relatedResourceClass, relatedSpec);
	}

	public QuerySpec duplicate() {
		QuerySpec copy = new QuerySpec(resourceClass);
		copy.pagingSpec = pagingSpec;
		copy.includedFields.addAll(includedFields);
		copy.includedRelations.addAll(includedRelations);
		copy.sort.addAll(sort);
		copy.filters.addAll(filters);

		Iterator<Entry<Class<?>, QuerySpec>> iterator = relatedSpecs.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Class<?>, QuerySpec> entry = iterator.next();
			copy.relatedSpecs.put(entry.getKey(), entry.getValue().duplicate());
		}
		return copy;
	}
}
