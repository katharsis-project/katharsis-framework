package io.katharsis.queryspec.repository;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryspec.FilterOperator;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.QuerySpecRelationshipRepository;
import io.katharsis.repository.LinksRepository;
import io.katharsis.repository.MetaRepository;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.repository.util.Relation;
import io.katharsis.response.LinksInformation;
import io.katharsis.response.MetaInformation;

public class TestQuerySpecRelationshipRepository
		implements QuerySpecRelationshipRepository<Task, Long, Project, Long>, MetaRepository<Project>, LinksRepository<Project> {

	private static final ConcurrentMap<Relation<Task>, Integer> THREAD_LOCAL_REPOSITORY = new ConcurrentHashMap<>();

	@Override
	public Class<Task> getSourceResourceClass() {
		return Task.class;
	}

	@Override
	public Class<Project> getTargetResourceClass() {
		return Project.class;
	}

	@Override
	public void setRelation(Task source, Long targetId, String fieldName) {
		removeRelations(fieldName);
		if (targetId != null) {
			THREAD_LOCAL_REPOSITORY.put(new Relation<>(source, targetId, fieldName), 0);
		}
	}

	@Override
	public void setRelations(Task source, Iterable<Long> targetIds, String fieldName) {
		removeRelations(fieldName);
		if (targetIds != null) {
			for (Long targetId : targetIds) {
				THREAD_LOCAL_REPOSITORY.put(new Relation<>(source, targetId, fieldName), 0);
			}
		}
	}

	@Override
	public void addRelations(Task source, Iterable<Long> targetIds, String fieldName) {
		for (Long targetId : targetIds) {
			THREAD_LOCAL_REPOSITORY.put(new Relation<>(source, targetId, fieldName), 0);
		}
	}

	@Override
	public void removeRelations(Task source, Iterable<Long> targetIds, String fieldName) {
		for (Long targetId : targetIds) {
			Iterator<Relation<Task>> iterator = THREAD_LOCAL_REPOSITORY.keySet().iterator();
			while (iterator.hasNext()) {
				Relation<Task> next = iterator.next();
				if (next.getFieldName().equals(fieldName) && next.getTargetId().equals(targetId)) {
					iterator.remove();
				}
			}
		}
	}

	public void removeRelations(String fieldName) {
		Iterator<Relation<Task>> iterator = THREAD_LOCAL_REPOSITORY.keySet().iterator();
		while (iterator.hasNext()) {
			Relation<Task> next = iterator.next();
			if (next.getFieldName().equals(fieldName)) {
				iterator.remove();
			}
		}
	}

	@Override
	public Project findOneTarget(Long sourceId, String fieldName, QuerySpec querySpec) {
		for (Relation<Task> relation : THREAD_LOCAL_REPOSITORY.keySet()) {
			if (relation.getSource().getId().equals(sourceId) && relation.getFieldName().equals(fieldName)) {
				Project project = new Project();
				project.setId((Long) relation.getTargetId());
				return project;
			}
		}
		return null;
	}

	@Override
	public Iterable<Project> findManyTargets(Long sourceId, String fieldName, QuerySpec querySpec) {
		List<Project> projects = new LinkedList<>();
		for (Relation<Task> relation : THREAD_LOCAL_REPOSITORY.keySet()) {
			if (relation.getSource().getId().equals(sourceId) && relation.getFieldName().equals(fieldName)) {
				Project project = new Project();
				project.setId((Long) relation.getTargetId());
				projects.add(project);
			}
		}
		if (querySpec == null) {
			return projects;
		}
		return querySpec.apply(projects);
	}

	@Override
	public Set<FilterOperator> getSupportedOperators() {
		Set<FilterOperator> set = new HashSet<FilterOperator>();
		set.add(FilterOperator.EQ);
		return set;
	}

	@Override
	public FilterOperator getDefaultOperator() {
		return FilterOperator.EQ;
	}

	@Override
	public LinksInformation getLinksInformation(Iterable<Project> resources, QueryParams queryParams) {
		return new LinksInformation() {

			public String name = "value";
		};
	}

	@Override
	public MetaInformation getMetaInformation(Iterable<Project> resources, QueryParams queryParams) {
		return new MetaInformation() {

			public String name = "value";
		};
	}

	public static void clear() {
		THREAD_LOCAL_REPOSITORY.clear();		
	}
}