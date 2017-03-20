package io.katharsis.resource.mock.repository;

import java.util.concurrent.ConcurrentHashMap;

import io.katharsis.errorhandling.exception.ResourceNotFoundException;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.repository.ResourceRepositoryV2;
import io.katharsis.resource.list.DefaultResourceList;
import io.katharsis.resource.list.ResourceList;
import io.katharsis.resource.mock.models.Group;

public class GroupRepository implements ResourceRepositoryV2<Group, Long> {

	private static final ConcurrentHashMap<Long, Group> THREAD_LOCAL_REPOSITORY = new ConcurrentHashMap<>();

	public static void clear() {
		THREAD_LOCAL_REPOSITORY.clear();
	}

	@Override
	public <S extends Group> S save(S entity) {
		entity.setId((long) (THREAD_LOCAL_REPOSITORY.size() + 1));
		THREAD_LOCAL_REPOSITORY.put(entity.getId(), entity);

		return entity;
	}

	@Override
	public Group findOne(Long aLong, QuerySpec queryParams) {
		Group group = THREAD_LOCAL_REPOSITORY.get(aLong);
		if (group == null) {
			throw new ResourceNotFoundException(Group.class.getCanonicalName());
		}

		return group;
	}

	@Override
	public ResourceList<Group> findAll(QuerySpec queryParams) {
		return queryParams.apply(THREAD_LOCAL_REPOSITORY.values());
	}

	@Override
	public ResourceList<Group> findAll(Iterable<Long> ids, QuerySpec queryParams) {
		DefaultResourceList<Group> values = new DefaultResourceList<>();
		for (Group value : THREAD_LOCAL_REPOSITORY.values()) {
			if (contains(value, ids)) {
				values.add(value);
			}
		}
		return values;
	}

	private boolean contains(Group value, Iterable<Long> ids) {
		for (Long id : ids) {
			if (value.getId().equals(id)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void delete(Long aLong) {
		THREAD_LOCAL_REPOSITORY.remove(aLong);
	}

	@Override
	public Class<Group> getResourceClass() {
		return Group.class;
	}

	@Override
	public <S extends Group> S create(S entity) {
		return save(entity);
	}
}
