package io.katharsis.queryspec.repository;

import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.QuerySpecLinksRepository;
import io.katharsis.queryspec.QuerySpecMetaRepository;
import io.katharsis.queryspec.QuerySpecRelationshipRepositoryBase;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.response.LinksInformation;
import io.katharsis.response.MetaInformation;

public class TaskToProjectRelationshipRepository extends QuerySpecRelationshipRepositoryBase<Task, Long, Project, Long>
		implements QuerySpecMetaRepository<Project>, QuerySpecLinksRepository<Project> {

	public TaskToProjectRelationshipRepository() {
		super(Task.class, Project.class);
	}

	@Override
	public LinksInformation getLinksInformation(Iterable<Project> resources, QuerySpec querySpec) {
		return new LinksInformation() {

			public String name = "value";
		};
	}

	@Override
	public MetaInformation getMetaInformation(Iterable<Project> resources, QuerySpec querySpec) {
		return new MetaInformation() {

			public String name = "value";
		};
	}

}