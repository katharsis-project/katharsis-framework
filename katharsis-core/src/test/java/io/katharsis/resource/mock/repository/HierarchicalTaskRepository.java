package io.katharsis.resource.mock.repository;

import java.util.HashMap;
import java.util.Map;

import io.katharsis.queryspec.QuerySpec;
import io.katharsis.repository.ResourceRepositoryBase;
import io.katharsis.resource.list.DefaultResourceList;
import io.katharsis.resource.mock.models.HierarchicalTask;

public class HierarchicalTaskRepository extends ResourceRepositoryBase<HierarchicalTask, Long> {

	private static Map<Long, HierarchicalTask> hierarchicalTasks = new HashMap<>();

	public HierarchicalTaskRepository() {
		super(HierarchicalTask.class);
	}

	@Override
	public DefaultResourceList<HierarchicalTask> findAll(QuerySpec querySpec) {
		return querySpec.apply(hierarchicalTasks.values());
	}

	@Override
	public <S extends HierarchicalTask> S save(S entity) {
		hierarchicalTasks.put(entity.getId(), entity);
		return null;
	}

	@Override
	public void delete(Long id) {
		hierarchicalTasks.remove(id);
	}

	public static void clear() {
		hierarchicalTasks.clear();
	}
}