package io.katharsis.itests.registry.fixtures.relationships;

import io.katharsis.repository.annotations.JsonApiResourceRepository;
import lombok.Data;

@Data
@JsonApiResourceRepository(value = Project.class)
public class ProjectRepo {
}
