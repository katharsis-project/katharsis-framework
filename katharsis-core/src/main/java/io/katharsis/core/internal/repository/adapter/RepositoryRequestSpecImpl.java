package io.katharsis.core.internal.repository.adapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.katharsis.core.internal.query.QuerySpecAdapter;
import io.katharsis.core.internal.utils.PreconditionUtil;
import io.katharsis.core.internal.utils.StringUtils;
import io.katharsis.legacy.internal.DefaultQuerySpecConverter;
import io.katharsis.legacy.internal.QueryParamsAdapter;
import io.katharsis.legacy.queryParams.DefaultQueryParamsParser;
import io.katharsis.legacy.queryParams.QueryParams;
import io.katharsis.legacy.queryParams.QueryParamsBuilder;
import io.katharsis.module.ModuleRegistry;
import io.katharsis.queryspec.IncludeFieldSpec;
import io.katharsis.queryspec.IncludeRelationSpec;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.repository.request.HttpMethod;
import io.katharsis.repository.request.QueryAdapter;
import io.katharsis.repository.request.RepositoryRequestSpec;
import io.katharsis.resource.information.ResourceField;
import io.katharsis.resource.information.ResourceInformation;

/**
 * Add some point maybe a more prominent api is necessary for this. But i likely
 * should be keept separate from QuerySpec.
 */
class RepositoryRequestSpecImpl implements RepositoryRequestSpec {

	private ResourceField relationshipField;

	private QueryAdapter queryAdapter;

	private Iterable<?> ids;

	private Object entity;

	private ModuleRegistry moduleRegistry;

	private HttpMethod method;

	private RepositoryRequestSpecImpl(ModuleRegistry moduleRegistry) {
		this.moduleRegistry = moduleRegistry;

	}

	@Override
	public HttpMethod getMethod() {
		return method;
	}

	@Override
	public QueryAdapter getQueryAdapter() {
		return queryAdapter;
	}

	@Override
	public ResourceField getRelationshipField() {
		return relationshipField;
	}

	@Override
	public QuerySpec getQuerySpec(ResourceInformation targetResourceInformation) {
		if (queryAdapter == null)
			return null;
		Class<?> targetResourceClass = targetResourceInformation.getResourceClass();
		if (queryAdapter instanceof QuerySpecAdapter) {
			QuerySpec querySpec = ((QuerySpecAdapter) queryAdapter).getQuerySpec();
			return querySpec.getOrCreateQuerySpec(targetResourceClass);
		}
		QueryParams queryParams = getQueryParams();
		DefaultQuerySpecConverter converter = new DefaultQuerySpecConverter(moduleRegistry);
		return converter.fromParams(targetResourceClass, queryParams);
	}

	@Override
	public QueryParams getQueryParams() {
		if (queryAdapter == null)
			return null;
		if (!(queryAdapter instanceof QueryParamsAdapter)) {
			QuerySpec rootQuerySpec = ((QuerySpecAdapter) queryAdapter).getQuerySpec();
			return convertToQueryParams(rootQuerySpec);
		}
		return ((QueryParamsAdapter) queryAdapter).getQueryParams();
	}

	private QueryParams convertToQueryParams(QuerySpec rootQuerySpec) {
		Map<String, Set<String>> map = new HashMap<>();
		List<QuerySpec> querySpecs = new ArrayList<>();
		querySpecs.addAll(rootQuerySpec.getRelatedSpecs().values());
		querySpecs.add(rootQuerySpec);
		for (QuerySpec spec : querySpecs) {
			if (!spec.getFilters().isEmpty() || !spec.getSort().isEmpty() || spec.getLimit() != null || spec.getOffset() != 0) {
				throw new UnsupportedOperationException(); // not
															// implemented
			}

			String resourceType = moduleRegistry.getResourceRegistry().findEntry(spec.getResourceClass()).getResourceInformation().getResourceType();
			if (!spec.getIncludedFields().isEmpty()) {
				Set<String> fieldNames = new HashSet<>();
				for (IncludeFieldSpec field : spec.getIncludedFields()) {
					fieldNames.add(StringUtils.join(".", field.getAttributePath()));
				}
				map.put("fields[" + resourceType + "]", fieldNames);
			}

			if (!spec.getIncludedRelations().isEmpty()) {
				Set<String> fieldNames = new HashSet<>();
				for (IncludeRelationSpec field : spec.getIncludedRelations()) {
					fieldNames.add(StringUtils.join(".", field.getAttributePath()));
				}
				map.put("include[" + resourceType + "]", fieldNames);
			}
		}

		QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());
		return queryParamsBuilder.buildQueryParams(map);
	}

	@Override
	public Serializable getId() {
		Iterable<Object> iterable = getIds();
		if (iterable != null) {
			Iterator<?> iterator = iterable.iterator();
			if (iterator.hasNext()) {
				return (Serializable) iterator.next();
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Iterable<T> getIds() {
		if (ids == null && entity != null) {
			ResourceInformation resourceInformation = queryAdapter.getResourceInformation();
			return (Iterable<T>) Collections.singleton(resourceInformation.getId(entity));
		}
		return (Iterable<T>) ids;
	}

	@Override
	public Object getEntity() {
		return entity;
	}

	public static RepositoryRequestSpec forDelete(ModuleRegistry moduleRegistry, QueryAdapter queryAdapter, Serializable id) {
		RepositoryRequestSpecImpl spec = new RepositoryRequestSpecImpl(moduleRegistry);
		spec.queryAdapter = queryAdapter;
		spec.ids = Arrays.asList(id);
		spec.method = HttpMethod.DELETE;
		return spec;
	}

	public static RepositoryRequestSpec forSave(ModuleRegistry moduleRegistry, HttpMethod method, QueryAdapter queryAdapter, Object entity) {
		RepositoryRequestSpecImpl spec = new RepositoryRequestSpecImpl(moduleRegistry);
		spec.queryAdapter = queryAdapter;
		spec.entity = entity;
		spec.method = method;
		return spec;
	}

	public static RepositoryRequestSpec forFindIds(ModuleRegistry moduleRegistry, QueryAdapter queryAdapter, Iterable<?> ids) {
		RepositoryRequestSpecImpl spec = new RepositoryRequestSpecImpl(moduleRegistry);
		spec.queryAdapter = queryAdapter;
		spec.ids = ids;
		spec.method = HttpMethod.GET;
		return spec;
	}

	public static RepositoryRequestSpec forFindAll(ModuleRegistry moduleRegistry, QueryAdapter queryAdapter) {
		RepositoryRequestSpecImpl spec = new RepositoryRequestSpecImpl(moduleRegistry);
		spec.queryAdapter = queryAdapter;
		spec.method = HttpMethod.GET;
		return spec;
	}

	public static RepositoryRequestSpec forFindId(ModuleRegistry moduleRegistry, QueryAdapter queryAdapter, Serializable id) {
		RepositoryRequestSpecImpl spec = new RepositoryRequestSpecImpl(moduleRegistry);
		spec.queryAdapter = queryAdapter;
		spec.ids = Collections.singleton(id);
		spec.method = HttpMethod.GET;
		return spec;
	}

	public static RepositoryRequestSpec forFindTarget(ModuleRegistry moduleRegistry, QueryAdapter queryAdapter, List<?> ids, ResourceField relationshipField) {
		RepositoryRequestSpecImpl spec = new RepositoryRequestSpecImpl(moduleRegistry);
		spec.queryAdapter = queryAdapter;
		spec.ids = ids;
		spec.relationshipField = relationshipField;
		spec.method = HttpMethod.GET;
		PreconditionUtil.assertNotNull("relationshipField is null", relationshipField);
		return spec;
	}

	public static RepositoryRequestSpecImpl forRelation(ModuleRegistry moduleRegistry, HttpMethod method, Object entity, QueryAdapter queryAdapter, Iterable<?> ids, ResourceField relationshipField) {
		RepositoryRequestSpecImpl spec = new RepositoryRequestSpecImpl(moduleRegistry);
		spec.entity = entity;
		spec.queryAdapter = queryAdapter;
		spec.ids = ids;
		spec.relationshipField = relationshipField;
		spec.method = method;
		PreconditionUtil.assertNotNull("relationshipField is null", relationshipField);
		return spec;
	}

}