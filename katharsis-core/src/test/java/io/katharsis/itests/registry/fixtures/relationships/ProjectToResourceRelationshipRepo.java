package io.katharsis.itests.registry.fixtures.relationships;

import io.katharsis.repository.annotations.JsonApiRelationshipRepository;

@JsonApiRelationshipRepository(source = Project.class, target = Task.class)
public class ProjectToResourceRelationshipRepo {
}
