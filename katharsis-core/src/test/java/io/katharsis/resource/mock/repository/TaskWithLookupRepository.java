package io.katharsis.resource.mock.repository;

import io.katharsis.repository.annotations.JsonApiFindOne;
import io.katharsis.repository.annotations.JsonApiResourceRepository;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.TaskWithLookup;

@JsonApiResourceRepository(TaskWithLookup.class)
public class TaskWithLookupRepository {

    @JsonApiFindOne
    public TaskWithLookup findOne(String id) {
        return new TaskWithLookup()
            .setId(id)
            .setProject(new Project().setId(42L))
            .setProjectOverridden(new Project().setId(42L));
    }
}
