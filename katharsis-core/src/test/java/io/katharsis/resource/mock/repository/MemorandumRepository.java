package io.katharsis.resource.mock.repository;

import java.util.concurrent.ConcurrentHashMap;

import io.katharsis.errorhandling.exception.ResourceNotFoundException;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.repository.ResourceRepositoryV2;
import io.katharsis.resource.list.DefaultResourceList;
import io.katharsis.resource.list.ResourceList;
import io.katharsis.resource.mock.models.Memorandum;

public class MemorandumRepository implements ResourceRepositoryV2<Memorandum, Long> {

	private static final ConcurrentHashMap<Long, Memorandum> THREAD_LOCAL_REPOSITORY = new ConcurrentHashMap<>();

	public static void clear() {
		THREAD_LOCAL_REPOSITORY.clear();
	}

	@Override
	public <S extends Memorandum> S save(S entity) {
		entity.setId((long) (THREAD_LOCAL_REPOSITORY.size() + 1));
		THREAD_LOCAL_REPOSITORY.put(entity.getId(), entity);

		return entity;
	}

	@Override
	public Memorandum findOne(Long aLong, QuerySpec queryParams) {
		Memorandum memorandum = THREAD_LOCAL_REPOSITORY.get(aLong);
		if (memorandum == null) {
			throw new ResourceNotFoundException(Memorandum.class.getCanonicalName());
		}

		return memorandum;
	}

	@Override
	public ResourceList<Memorandum> findAll(QuerySpec queryParams) {
		return queryParams.apply(THREAD_LOCAL_REPOSITORY.values());
	}

	@Override
	public ResourceList<Memorandum> findAll(Iterable<Long> ids, QuerySpec queryParams) {
		DefaultResourceList<Memorandum> values = new DefaultResourceList<>();
		for (Memorandum value : THREAD_LOCAL_REPOSITORY.values()) {
			if (contains(value, ids)) {
				values.add(value);
			}
		}
		return values;
	}

	private boolean contains(Memorandum value, Iterable<Long> ids) {
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
	public Class<Memorandum> getResourceClass() {
		return Memorandum.class;
	}

	@Override
	public <S extends Memorandum> S create(S entity) {
		return save(entity);
	}
}
