package io.katharsis.queryspec.internal;

import io.katharsis.queryParams.include.Inclusion;
import io.katharsis.queryParams.params.IncludedFieldsParams;
import io.katharsis.queryParams.params.IncludedRelationsParams;
import io.katharsis.queryParams.params.TypedParams;
import io.katharsis.queryspec.IncludeFieldSpec;
import io.katharsis.queryspec.IncludeRelationSpec;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.utils.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class QuerySpecAdapter implements QueryAdapter {

	private QuerySpec querySpec;

	private ResourceRegistry resourceRegistry;

	private PageQueryAdapter pageQuerySpecAdapter;

	public QuerySpecAdapter(QuerySpec querySpec, ResourceRegistry resourceRegistry) {
		this.querySpec = querySpec;
		this.resourceRegistry = resourceRegistry;
		this.pageQuerySpecAdapter = new PageQuerySpecAdapter(querySpec.getPagingSpec());
	}

	public QuerySpecAdapter(QuerySpec querySpec, ResourceRegistry resourceRegistry, PageQuerySpecAdapter pageQuerySpecAdapter) {
		this.querySpec = querySpec;
		this.resourceRegistry = resourceRegistry;
		this.pageQuerySpecAdapter = pageQuerySpecAdapter;
	}

	public QuerySpec getQuerySpec() {
		return querySpec;
	}

	@Override
	public boolean hasIncludedRelations() {
		if (!querySpec.getIncludedRelations().isEmpty()) {
			return true;
		}
		for (QuerySpec relatedSpec : querySpec.getRelatedSpecs().values()) {
			if (!relatedSpec.getIncludedRelations().isEmpty()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public TypedParams<IncludedRelationsParams> getIncludedRelations() {
		Map<String, IncludedRelationsParams> params = new HashMap<>();
		addRelations(params, querySpec);
		for (QuerySpec relatedSpec : querySpec.getRelatedSpecs().values()) {
			addRelations(params, relatedSpec);
		}
		return new TypedParams<>(params);
	}

	private void addRelations(Map<String, IncludedRelationsParams> params, QuerySpec spec) {
		if (!spec.getIncludedRelations().isEmpty()) {
			Set<Inclusion> set = new HashSet<>();
			for (IncludeRelationSpec relation : spec.getIncludedRelations()) {
				set.add(new Inclusion(StringUtils.join(".", relation.getAttributePath())));
			}
			params.put(getResourceTypee(spec), new IncludedRelationsParams(set));
		}
	}

	private String getResourceTypee(QuerySpec spec) {
		RegistryEntry<?> entry = resourceRegistry.getEntry(spec.getResourceClass());
		ResourceInformation resourceInformation = entry.getResourceInformation();
		return resourceInformation.getResourceType();
	}

	@Override
	public TypedParams<IncludedFieldsParams> getIncludedFields() {
		Map<String, IncludedFieldsParams> params = new HashMap<>();
		addFields(params, querySpec);
		for (QuerySpec relatedSpec : querySpec.getRelatedSpecs().values()) {
			addFields(params, relatedSpec);
		}
		return new TypedParams<>(params);
	}

	private void addFields(Map<String, IncludedFieldsParams> params, QuerySpec spec) {
		if (!spec.getIncludedFields().isEmpty()) {
			Set<String> set = new HashSet<>();
			for (IncludeFieldSpec relation : spec.getIncludedFields()) {
				set.add(StringUtils.join(".", relation.getAttributePath()));
			}
			params.put(getResourceTypee(spec), new IncludedFieldsParams(set));
		}
	}

	@Override
	public Class<?> getResourceClass() {
		return querySpec.getResourceClass();
	}

	@Override
	public QueryAdapter duplicate() {
		return new QuerySpecAdapter(querySpec.duplicate(), resourceRegistry, (PageQuerySpecAdapter) pageQuerySpecAdapter.duplicate());
	}

	@Override
	public PageQueryAdapter getPageAdapter() {
		return this.pageQuerySpecAdapter;
	}

	@Override
	public void setPageAdapter(PageQueryAdapter adapter) {
		if (!(adapter instanceof PageQuerySpecAdapter))
			throw new UnsupportedOperationException("QuerySpecAdapter only supports PageQuerySpecAdapters and derivative types");
		this.pageQuerySpecAdapter = adapter;
		PageQuerySpecAdapter pageAdapter = (PageQuerySpecAdapter) adapter;
		querySpec.setPagingSpec(pageAdapter.getPagingSpec());
	}
}
