package io.katharsis.resource.registry;

import io.katharsis.repository.RelationshipRepository;
import io.katharsis.repository.exception.RelationshipRepositoryNotFoundException;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.models.User;
import io.katharsis.resource.mock.repository.TaskToProjectRepository;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.Field;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class RegistryEntryTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

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
    public void onInvalidRelationshipClassShouldThrowException() throws Exception {
        // GIVEN
        ResourceInformation resourceInformation = new ResourceInformation(Task.class, null, null, null);
        RegistryEntry<Task> sut = new RegistryEntry<>(resourceInformation, null,
            Collections.singletonList(new TaskToProjectRepository()));

        // THEN
        expectedException.expect(RelationshipRepositoryNotFoundException.class);

        // WHEN
        sut.getRelationshipRepositoryForClass(User.class);
    }

    @Test
    public void equalsContract() throws NoSuchFieldException {
        EqualsVerifier.forClass(RegistryEntry.class)
                .withPrefabValues(Field.class, String.class.getDeclaredField("value"), String.class.getDeclaredField("hash"))
                .usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }
}
