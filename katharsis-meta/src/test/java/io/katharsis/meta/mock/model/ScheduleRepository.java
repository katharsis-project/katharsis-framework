package io.katharsis.meta.mock.model;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import io.katharsis.queryspec.QuerySpec;
import io.katharsis.repository.ResourceRepositoryV2;
import io.katharsis.resource.links.DefaultPagedLinksInformation;
import io.katharsis.resource.links.LinksInformation;
import io.katharsis.resource.list.ResourceListBase;
import io.katharsis.resource.meta.MetaInformation;

@Path("schedules")
public interface ScheduleRepository extends ResourceRepositoryV2<Schedule, Long> {

	@GET
	@Path("repositoryAction")
	public String repositoryAction(@QueryParam(value = "msg") String msg);

	@GET
	@Path("{id}/resourceAction")
	public String resourceAction(@PathParam("id") long id, @QueryParam(value = "msg") String msg);

	@Override
	public ScheduleList findAll(QuerySpec querySpec);

	class ScheduleList extends ResourceListBase<Schedule, ScheduleListMeta, ScheduleListLinks> {

	}

	class ScheduleListLinks extends DefaultPagedLinksInformation implements LinksInformation {

		public String name = "value";
	}

	class ScheduleListMeta implements MetaInformation {

		public String name = "value";

	}
}
