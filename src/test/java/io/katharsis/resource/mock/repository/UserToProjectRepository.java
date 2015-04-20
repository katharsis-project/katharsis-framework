package io.katharsis.resource.mock.repository;

import io.katharsis.repository.RelationshipRepository;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.User;

import java.util.Collections;

public class UserToProjectRepository implements RelationshipRepository<User, Long, Project> {
    @Override
    public void addRelation(User source, Project target, String fieldName) {

    }

    @Override
    public void removeRelation(User source, Project target, String fieldName) {

    }

    @Override
    public Project findOneTarget(Long sourceId, String fieldName) {
        return null;
    }

    @Override
    public Iterable<Project> findTargets(Long sourceId, String fieldName) {
        return Collections.singletonList(new Project());
    }
}
