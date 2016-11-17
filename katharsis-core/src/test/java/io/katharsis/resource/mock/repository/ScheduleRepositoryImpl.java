package io.katharsis.resource.mock.repository;

import java.util.HashMap;
import java.util.Map;

import io.katharsis.queryspec.QuerySpec;
import io.katharsis.repository.ResourceRepositoryBase;
import io.katharsis.resource.mock.models.Schedule;

public class ScheduleRepositoryImpl extends ResourceRepositoryBase<Schedule, Long> implements ScheduleRepository {

	private static Map<Long, Schedule> schedules = new HashMap<>();

	public ScheduleRepositoryImpl() {
		super(Schedule.class);
	}

	@Override
	public ScheduleList findAll(QuerySpec querySpec) {
		ScheduleList list = new ScheduleList();
		list.addAll(querySpec.apply(schedules.values()));
		list.setLinks(new ScheduleListLinks());
		list.setMeta(new ScheduleListMeta());
		return list;
	}

	@Override
	public <S extends Schedule> S save(S entity) {
		schedules.put(entity.getId(), entity);
		return null;
	}

	@Override
	public void delete(Long id) {
		schedules.remove(id);
	}

	public static void clear() {
		schedules.clear();
	}
}