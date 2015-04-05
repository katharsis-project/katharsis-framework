package io.katharsis.resource.registry;

import io.katharsis.repository.RelationshipRepository;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.models.User;
import io.katharsis.resource.mock.repository.TaskToProjectRepository;
import org.junit.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class RegistryEntryTest {

    @Test
    public void onValidRelationshipClassShouldReturnRelationshipRepository() throws Exception {
        // GIVEN
        RegistryEntry<Task> sut = new RegistryEntry<>(null, null, Collections.singletonList(new TaskToProjectRepository()));

        // WHEN
        RelationshipRepository<Task, ?, ?, ?> relationshipRepository = sut.getRelationshipRepositoryForClass(Project.class);

        // THEN
        assertThat(relationshipRepository).isExactlyInstanceOf(TaskToProjectRepository.class);
    }

    @Test
    public void onInvalidRelationshipClassShouldReturnNull() throws Exception {
        // GIVEN
        RegistryEntry<Task> sut = new RegistryEntry<>(null, null, Collections.singletonList(new TaskToProjectRepository()));

        // WHEN
        RelationshipRepository<Task, ?, ?, ?> relationshipRepository = sut.getRelationshipRepositoryForClass(User.class);

        // THEN
        assertThat(relationshipRepository).isNull();
    }
}
