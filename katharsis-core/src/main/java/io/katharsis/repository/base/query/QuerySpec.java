package io.katharsis.repository.base.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class QuerySpec {

	private Long limit = null;
	private Long offset = null;
	private List<FilterSpec> filters = new ArrayList<FilterSpec>();
	private List<SortSpec> sort = new ArrayList<SortSpec>();
	private List<IncludeFieldSpec> includedFields = new ArrayList<IncludeFieldSpec>();
	private List<IncludeRelationSpec> includedRelations = new ArrayList<IncludeRelationSpec>();
	private Map<Class<?>, QuerySpec> relatedSpecs = new HashMap<Class<?>, QuerySpec>();

	public void accept(QuerySpecVisitor visitor) {
		if (visitor.visit(this)) {
			if (offset != null) {
				visitor.visitOffset(offset.longValue());
			}
			if (limit != null) {
				visitor.visitLimit(limit.longValue());
			}
			for (FilterSpec filterSpec : filters) {
				visitor.visit(filterSpec);
			}
			for (SortSpec sortSpec : sort) {
				visitor.visit(sortSpec);
			}
			for (IncludeFieldSpec include : includedFields) {
				visitor.visit(include);
			}
			for (IncludeRelationSpec include : includedRelations) {
				visitor.visit(include);
			}
			for (Entry<Class<?>, QuerySpec> entry : relatedSpecs.entrySet()) {
				visitor.visit(entry.getKey(), entry.getValue());
			}
		}
	}

	public Long getLimit() {
		return limit;
	}

	public void setLimit(Long limit) {
		this.limit = limit;
	}

	public Long getOffset() {
		return offset;
	}

	public void setOffset(Long offset) {
		this.offset = offset;
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
}
