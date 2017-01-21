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

import io.katharsis.core.internal.query.DefaultQuerySpecConverter;
import io.katharsis.core.internal.query.QueryParamsAdapter;
import io.katharsis.core.internal.query.QuerySpecAdapter;
import io.katharsis.core.internal.utils.PreconditionUtil;
import io.katharsis.core.internal.utils.StringUtils;
import io.katharsis.queryParams.DefaultQueryParamsParser;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.queryspec.IncludeFieldSpec;
import io.katharsis.queryspec.IncludeRelationSpec;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.repository.request.HttpMethod;
import io.katharsis.repository.request.RepositoryRequestSpec;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;

/**
 * Add some point maybe a more prominent api is necessary for this. But i likely
 * should be keept separate from QuerySpec.
 */
class RepositoryRequestSpecImpl implements RepositoryRequestSpec {

	private String relationshipField;

	private Class<?> relationshipSourceClass;

	private QueryAdapter queryAdapter;

	private Iterable<?> ids;

	private Object entity;

	private ResourceRegistry resourceRegistry;

	private HttpMethod method;

	private RepositoryRequestSpecImpl(ResourceRegistry resourceRegistry) {
		this.resourceRegistry = resourceRegistry;
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
	public String getRelationshipField() {
		return relationshipField;
	}

	@Override
	public Class<?> getRelationshipSourceClass() {
		return relationshipSourceClass;
	}

	@Override
	public QuerySpec getQuerySpec(Class<?> targetResourceClass) {
		if (queryAdapter == null)
			return null;
		if (queryAdapter instanceof QuerySpecAdapter) {
			QuerySpec querySpec = ((QuerySpecAdapter) queryAdapter).getQuerySpec();
			return querySpec.getOrCreateQuerySpec(targetResourceClass);
		}
		QueryParams queryParams = getQueryParams();
		DefaultQuerySpecConverter converter = new DefaultQuerySpecConverter(resourceRegistry);
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

			String resourceType = resourceRegistry.getResourceType(spec.getResourceClass());
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
			RegistryEntry<?> entry = resourceRegistry.getEntry(queryAdapter.getResourceClass());
			ResourceInformation resourceInformation = entry.getResourceInformation();
			return (Iterable<T>) Collections.singleton(resourceInformation.getId(entity));
		}
		return (Iterable<T>) ids;
	}

	@Override
	public Object getEntity() {
		return entity;
	}

	public static RepositoryRequestSpec forDelete(ResourceRegistry resourceRegistry, QueryAdapter queryAdapter, Serializable id) {
		RepositoryRequestSpecImpl spec = new RepositoryRequestSpecImpl(resourceRegistry);
		spec.queryAdapter = queryAdapter;
		spec.ids = Arrays.asList(id);
		spec.method = HttpMethod.DELETE;
		return spec;
	}

	public static RepositoryRequestSpec forSave(ResourceRegistry resourceRegistry, HttpMethod method, QueryAdapter queryAdapter, Object entity) {
		RepositoryRequestSpecImpl spec = new RepositoryRequestSpecImpl(resourceRegistry);
		spec.queryAdapter = queryAdapter;
		spec.entity = entity;
		spec.method = method;
		return spec;
	}

	public static RepositoryRequestSpec forFindIds(ResourceRegistry resourceRegistry, QueryAdapter queryAdapter, Iterable<?> ids) {
		RepositoryRequestSpecImpl spec = new RepositoryRequestSpecImpl(resourceRegistry);
		spec.queryAdapter = queryAdapter;
		spec.ids = ids;
		spec.method = HttpMethod.GET;
		return spec;
	}

	public static RepositoryRequestSpec forFindAll(ResourceRegistry resourceRegistry, QueryAdapter queryAdapter) {
		RepositoryRequestSpecImpl spec = new RepositoryRequestSpecImpl(resourceRegistry);
		spec.queryAdapter = queryAdapter;
		spec.method = HttpMethod.GET;
		return spec;
	}

	public static RepositoryRequestSpec forFindId(ResourceRegistry resourceRegistry, QueryAdapter queryAdapter, Serializable id) {
		RepositoryRequestSpecImpl spec = new RepositoryRequestSpecImpl(resourceRegistry);
		spec.queryAdapter = queryAdapter;
		spec.ids = Collections.singleton(id);
		spec.method = HttpMethod.GET;
		return spec;
	}

	public static RepositoryRequestSpec forFindTarget(ResourceRegistry resourceRegistry, QueryAdapter queryAdapter, List<?> ids, String relationshipField, Class<?> resourceClass) {
		RepositoryRequestSpecImpl spec = new RepositoryRequestSpecImpl(resourceRegistry);
		spec.queryAdapter = queryAdapter;
		spec.ids = ids;
		spec.relationshipField = relationshipField;
		spec.relationshipSourceClass = resourceClass;
		spec.method = HttpMethod.GET;
		PreconditionUtil.assertNotNull("relationshipField is null", relationshipField);
		PreconditionUtil.assertNotNull("relationshipSourceClass is null", resourceClass);
		return spec;
	}

	public static RepositoryRequestSpecImpl forRelation(ResourceRegistry resourceRegistry, HttpMethod method, Object entity, QueryAdapter queryAdapter, Iterable<?> ids, String relationshipField,
			Class<?> relationshipSourceClass) {
		RepositoryRequestSpecImpl spec = new RepositoryRequestSpecImpl(resourceRegistry);
		spec.entity = entity;
		spec.queryAdapter = queryAdapter;
		spec.ids = ids;
		spec.relationshipField = relationshipField;
		spec.relationshipSourceClass = relationshipSourceClass;
		spec.method = method;
		PreconditionUtil.assertNotNull("relationshipField is null", relationshipField);
		PreconditionUtil.assertNotNull("relationshipSourceClass is null", relationshipSourceClass);
		return spec;
	}

}