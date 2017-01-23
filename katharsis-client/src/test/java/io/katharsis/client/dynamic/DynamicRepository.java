package io.katharsis.client.dynamic;

import java.util.HashMap;
import java.util.Map;

import io.katharsis.queryspec.QuerySpec;
import io.katharsis.repository.ResourceRepositoryBase;
import io.katharsis.repository.UntypedResourceRepository;
import io.katharsis.resource.Resource;
import io.katharsis.resource.list.DefaultResourceList;

public class DynamicRepository extends ResourceRepositoryBase<Resource, String> implements UntypedResourceRepository {

	private static Map<String, Resource> RESOURCES = new HashMap<>();

	public DynamicRepository() {
		super(Resource.class);
	}

	@Override
	public String getResourceType() {
		return "dynamic";
	}

	@Override
	public Class<Resource> getResourceClass() {
		return Resource.class;
	}

	@Override
	public DefaultResourceList<Resource> findAll(QuerySpec querySpec) {
		return querySpec.apply(RESOURCES.values());
	}

	@Override
	public <S extends Resource> S create(S entity) {
		return save(entity);
	}

	@Override
	public <S extends Resource> S save(S entity) {
		RESOURCES.put(entity.getId(), entity);
		return entity;
	}

	@Override
	public void delete(String id) {
		RESOURCES.remove(id);
	}

	public static void clear() {
		RESOURCES.clear();
	}
}