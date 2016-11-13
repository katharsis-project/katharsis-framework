package io.katharsis.client.mock.repository;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import io.katharsis.client.mock.models.Schedule;
import io.katharsis.queryspec.QuerySpecResourceRepository;

@Path("schedules")
public interface ScheduleRepository extends QuerySpecResourceRepository<Schedule, Long> {

	@GET
	@Path("repositoryAction")
	public String repositoryAction(@QueryParam(value = "msg") String msg);

	@GET
	@Path("{id}/resourceAction")
	public Response resourceAction(@PathParam("param") String msg);
}
