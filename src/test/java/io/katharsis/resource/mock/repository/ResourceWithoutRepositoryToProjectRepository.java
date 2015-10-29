package io.katharsis.resource.mock.repository;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.RelationshipRepository;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.ResourceWithoutRepository;

public class ResourceWithoutRepositoryToProjectRepository
    implements RelationshipRepository<ResourceWithoutRepository, String, Project, Long> {
    @Override
    public void setRelation(ResourceWithoutRepository source, Long targetId, String fieldName) {

    }

    @Override
    public void setRelations(ResourceWithoutRepository source, Iterable<Long> targetIds, String fieldName) {

    }

    @Override
    public void addRelations(ResourceWithoutRepository source, Iterable<Long> targetIds, String fieldName) {

    }

    @Override
    public void removeRelations(ResourceWithoutRepository source, Iterable<Long> targetIds, String fieldName) {

    }

    @Override
    public Project findOneTarget(String sourceId, String fieldName, QueryParams queryParams) {
        return null;
    }

    @Override
    public Iterable<Project> findManyTargets(String sourceId, String fieldName, QueryParams queryParams) {
        return null;
    }
}
