package io.katharsis.core.internal.query;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.katharsis.core.internal.utils.StringUtils;
import io.katharsis.legacy.queryParams.include.Inclusion;
import io.katharsis.legacy.queryParams.params.IncludedFieldsParams;
import io.katharsis.legacy.queryParams.params.IncludedRelationsParams;
import io.katharsis.legacy.queryParams.params.TypedParams;
import io.katharsis.queryspec.IncludeFieldSpec;
import io.katharsis.queryspec.IncludeRelationSpec;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.repository.request.QueryAdapter;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;

public class QuerySpecAdapter implements QueryAdapter {

	private QuerySpec querySpec;

	private ResourceRegistry resourceRegistry;

	public QuerySpecAdapter(QuerySpec querySpec, ResourceRegistry resourceRegistry) {
		this.querySpec = querySpec;
		this.resourceRegistry = resourceRegistry;
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
		RegistryEntry entry = resourceRegistry.findEntry(spec.getResourceClass());
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
	public ResourceInformation getResourceInformation() {
		return resourceRegistry.findEntry(querySpec.getResourceClass()).getResourceInformation();
	}

	@Override
	public Long getLimit() {
		return querySpec.getLimit();
	}

	@Override
	public long getOffset() {
		return querySpec.getOffset();
	}

	@Override
	public QueryAdapter duplicate() {
		return new QuerySpecAdapter(querySpec.duplicate(), resourceRegistry);
	}

	@Override
	public void setLimit(Long limit) {
		querySpec.setLimit(limit);
	}

	@Override
	public void setOffset(long offset) {
		querySpec.setOffset(offset);
	}
}
