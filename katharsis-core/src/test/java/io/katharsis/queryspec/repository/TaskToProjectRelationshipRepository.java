package io.katharsis.queryspec.repository;

import io.katharsis.queryspec.QuerySpec;
import io.katharsis.repository.RelationshipRepositoryBase;
import io.katharsis.repository.LinksRepositoryV2;
import io.katharsis.repository.MetaRepositoryV2;
import io.katharsis.resource.links.LinksInformation;
import io.katharsis.resource.meta.MetaInformation;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;

public class TaskToProjectRelationshipRepository extends RelationshipRepositoryBase<Task, Long, Project, Long>
		implements MetaRepositoryV2<Project>, LinksRepositoryV2<Project> {

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