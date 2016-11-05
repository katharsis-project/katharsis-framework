package io.katharsis.resource.registry.responseRepository;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryspec.DefaultQuerySpecConverter;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.queryspec.internal.QueryParamsAdapter;
import io.katharsis.queryspec.internal.QuerySpecAdapter;
import io.katharsis.repository.filter.RepositoryFilterContext;
import io.katharsis.request.repository.RepositoryRequestSpec;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.utils.PreconditionUtil;

/**
 * Add some point maybe a more prominent api is necessary for this. But i likely should
 * be keept separate from QuerySpec.
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
		return ((QueryParamsAdapter) queryAdapter).getQueryParams();
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
		if(ids == null && entity != null){
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

	public static RepositoryRequestSpec forSave(ResourceRegistry resourceRegistry, HttpMethod method, QueryAdapter queryAdapter,
			Object entity) {
		RepositoryRequestSpecImpl spec = new RepositoryRequestSpecImpl(resourceRegistry);
		spec.queryAdapter = queryAdapter;
		spec.entity = entity;
		spec.method = method;
		return spec;
	}

	public static RepositoryRequestSpec forFindIds(ResourceRegistry resourceRegistry, QueryAdapter queryAdapter,
			Iterable<?> ids) {
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

	public static RepositoryRequestSpec forFindTarget(ResourceRegistry resourceRegistry, QueryAdapter queryAdapter, List<?> ids,
			String relationshipField, Class<?> resourceClass) {
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

	public static RepositoryRequestSpecImpl forRelation(ResourceRegistry resourceRegistry, HttpMethod method, Object entity,
			QueryAdapter queryAdapter, Iterable<?> ids, String relationshipField, Class<?> relationshipSourceClass) {
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