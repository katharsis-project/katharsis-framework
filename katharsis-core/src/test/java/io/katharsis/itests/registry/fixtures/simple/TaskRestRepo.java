package io.katharsis.itests.registry.fixtures.simple;

import io.katharsis.repository.annotations.JsonApiResourceRepository;
import lombok.Data;


@Data
@JsonApiResourceRepository(value = Task.class)
public class TaskRestRepo {
}