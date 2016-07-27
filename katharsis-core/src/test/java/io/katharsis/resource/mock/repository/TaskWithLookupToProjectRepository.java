package io.katharsis.resource.mock.repository;

import io.katharsis.repository.annotations.JsonApiFindOneTarget;
import io.katharsis.repository.annotations.JsonApiRelationshipRepository;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.TaskWithLookup;

@JsonApiRelationshipRepository(source = TaskWithLookup.class, target = Project.class)
public class TaskWithLookupToProjectRepository {

    @JsonApiFindOneTarget
    public Project findOneTarget(String sourceId, String fieldName) {
        return new Project()
            .setId(1L);
    }
}
