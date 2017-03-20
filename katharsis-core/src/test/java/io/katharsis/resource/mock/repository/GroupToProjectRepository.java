package io.katharsis.resource.mock.repository;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.katharsis.legacy.queryParams.QueryParams;
import io.katharsis.legacy.repository.RelationshipRepository;
import io.katharsis.resource.mock.models.Group;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.repository.util.Relation;

public class GroupToProjectRepository extends AbstractRelationShipRepository<Group> implements
		RelationshipRepository<Group, Long, Project, Long> {

	private final static ConcurrentMap<Relation<Group>, Integer> STATIC_REPOSITORY = new ConcurrentHashMap<>();

	public static void clear() {
		STATIC_REPOSITORY.clear();
	}

	@Override
	ConcurrentMap<Relation<Group>, Integer> getRepo() {
		return STATIC_REPOSITORY;
	}

	@Override
	public Project findOneTarget(Long sourceId, String fieldName, QueryParams queryParams) {
		return null;
	}

	@Override
	public Iterable<Project> findManyTargets(Long sourceId, String fieldName, QueryParams queryParams) {
		List<Project> projects = new LinkedList<>();
		for (Relation<Group> relation : getRepo().keySet()) {
			if (relation.getSource().getId().equals(sourceId) && relation.getFieldName().equals(fieldName)) {
				Project project = new Project();
				project.setId((Long) relation.getTargetId());
				projects.add(project);
			}
		}
		return projects;
	}
}
