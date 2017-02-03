package io.katharsis.resource.mock.repository;

import io.katharsis.queryspec.QuerySpec;
import io.katharsis.repository.ResourceRepositoryV2;
import io.katharsis.resource.links.DefaultPagedLinksInformation;
import io.katharsis.resource.links.LinksInformation;
import io.katharsis.resource.list.ResourceListBase;
import io.katharsis.resource.meta.MetaInformation;
import io.katharsis.resource.mock.models.Schedule;

public interface ScheduleRepository extends ResourceRepositoryV2<Schedule, Long> {

	class ScheduleList extends ResourceListBase<Schedule, ScheduleListMeta, ScheduleListLinks> {

	}

	class ScheduleListLinks extends DefaultPagedLinksInformation implements LinksInformation {

		public String name = "value";
	}

	class ScheduleListMeta implements MetaInformation {

		public String name = "value";

	}

	@Override
	public ScheduleList findAll(QuerySpec querySpec);
}
