package io.katharsis.resource.registry;

import io.katharsis.repository.RelationshipRepository;
import io.katharsis.repository.exception.RelationshipRepositoryNotFoundException;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.mock.models.*;
import io.katharsis.resource.mock.repository.TaskRepository;
import io.katharsis.resource.mock.repository.TaskToProjectRepository;
import io.katharsis.resource.registry.repository.DirectRelationshipEntry;
import io.katharsis.resource.registry.repository.DirectResourceEntry;
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
        RegistryEntry<Task> sut = new RegistryEntry<>(null, new DirectResourceEntry<>(new TaskRepository()), Collections.singletonList(new DirectRelationshipEntry<>(new TaskToProjectRepository())));

        // WHEN
        RelationshipRepository<Task, ?, ?, ?> relationshipRepository = sut.getRelationshipRepositoryForClass(Project.class, null);

        // THEN
        assertThat(relationshipRepository).isExactlyInstanceOf(TaskToProjectRepository.class);
    }

    @Test
    public void onInvalidRelationshipClassShouldThrowException() throws Exception {
        // GIVEN
        ResourceInformation resourceInformation = new ResourceInformation(Task.class, null, null, null);
        RegistryEntry<Task> sut = new RegistryEntry<>(resourceInformation, null,
            Collections.singletonList(new DirectRelationshipEntry<>(new TaskToProjectRepository())));

        // THEN
        expectedException.expect(RelationshipRepositoryNotFoundException.class);

        // WHEN
        sut.getRelationshipRepositoryForClass(User.class, null);
    }

    @Test
    public void onValidParentShouldReturnTrue() throws Exception {
        // GIVEN
        RegistryEntry<Thing> thing = new RegistryEntry<>(new ResourceInformation(Thing.class, null, null, null), null);
        RegistryEntry<Document> document = new RegistryEntry<>(new ResourceInformation(Document.class, null, null, null), null);
        document.setParentRegistryEntry(thing);
        RegistryEntry<Memorandum> memorandum = new RegistryEntry<>(new ResourceInformation(Memorandum.class, null, null, null), null);
        memorandum.setParentRegistryEntry(document);

        // WHEN
        boolean result = memorandum.isParent(thing);

        // THEN
        assertThat(result).isTrue();
    }

    @Test
    public void onInvalidParentShouldReturnFalse() throws Exception {
        // GIVEN
        RegistryEntry<Document> document = new RegistryEntry<>(new ResourceInformation(Document.class, null, null, null), null);
        RegistryEntry<Task> task = new RegistryEntry<>(new ResourceInformation(Task.class, null, null, null), null);

        // WHEN
        boolean result = document.isParent(task);

        // THEN
        assertThat(result).isFalse();
    }

    @Test
    public void equalsContract() throws NoSuchFieldException {
        RegistryEntry blue = new RegistryEntry(new ResourceInformation(String.class, null, null, null), null);
        RegistryEntry red = new RegistryEntry(new ResourceInformation(Long.class, null, null, null), null);
        EqualsVerifier.forClass(RegistryEntry.class)
                .withPrefabValues(RegistryEntry.class, blue, red)
                .withPrefabValues(Field.class, String.class.getDeclaredField("value"), String.class.getDeclaredField("hash"))
                .usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }
}
