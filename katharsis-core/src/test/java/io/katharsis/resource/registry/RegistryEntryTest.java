package io.katharsis.resource.registry;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.Collections;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import io.katharsis.locator.SampleJsonServiceLocator;
import io.katharsis.module.ModuleRegistry;
import io.katharsis.repository.RepositoryInstanceBuilder;
import io.katharsis.repository.exception.RelationshipRepositoryNotFoundException;
import io.katharsis.repository.information.ResourceRepositoryInformation;
import io.katharsis.repository.information.internal.ResourceRepositoryInformationImpl;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.mock.models.Document;
import io.katharsis.resource.mock.models.Memorandum;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.models.Thing;
import io.katharsis.resource.mock.models.User;
import io.katharsis.resource.mock.repository.TaskRepository;
import io.katharsis.resource.mock.repository.TaskToProjectRepository;
import io.katharsis.resource.registry.repository.AnnotatedResourceEntry;
import io.katharsis.resource.registry.repository.DirectResponseRelationshipEntry;
import io.katharsis.resource.registry.repository.adapter.RelationshipRepositoryAdapter;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

@SuppressWarnings("unchecked")
public class RegistryEntryTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void onValidRelationshipClassShouldReturnRelationshipRepository() throws Exception {
		// GIVEN
		RegistryEntry<Task> sut = new RegistryEntry(new ResourceRepositoryInformationImpl(null, null, null),
				new AnnotatedResourceEntry<>(new RepositoryInstanceBuilder(new SampleJsonServiceLocator(), TaskRepository.class)),
				Collections.singletonList(new DirectResponseRelationshipEntry<>(
						new RepositoryInstanceBuilder(new SampleJsonServiceLocator(), TaskToProjectRepository.class))));

		// WHEN
		RelationshipRepositoryAdapter<Task, ?, ?, ?> relationshipRepository = sut.getRelationshipRepositoryForClass(Project.class,
				null);

		// THEN
		assertThat(relationshipRepository).isExactlyInstanceOf(RelationshipRepositoryAdapter.class);
	}

	@Test
	public void onInvalidRelationshipClassShouldThrowException() throws Exception {
		// GIVEN
		ResourceRepositoryInformation resourceInformation = newRepositoryInformation(Task.class, "tasks");
		RegistryEntry<Task> sut = new RegistryEntry(resourceInformation, null,
				Collections.singletonList(new DirectResponseRelationshipEntry<>(
						new RepositoryInstanceBuilder(new SampleJsonServiceLocator(), TaskToProjectRepository.class))));

		// THEN
		expectedException.expect(RelationshipRepositoryNotFoundException.class);

		// WHEN
		sut.getRelationshipRepositoryForClass(User.class, null);
	}

	private <T> ResourceRepositoryInformation newRepositoryInformation(Class<T> repositoryClass, String path) {
		return new ResourceRepositoryInformationImpl(null, path, new ResourceInformation(Task.class, path, null));
	}

	@Test
	public void onValidParentShouldReturnTrue() throws Exception {
		// GIVEN
		RegistryEntry<Thing> thing = new RegistryEntry<>(newRepositoryInformation(Thing.class, "things"), null);
		RegistryEntry<Document> document = new RegistryEntry<>(newRepositoryInformation(Document.class, "documents"), null);
		document.setParentRegistryEntry(thing);
		RegistryEntry<Memorandum> memorandum = new RegistryEntry<>(newRepositoryInformation(Memorandum.class, "memorandum"),
				null);
		memorandum.setParentRegistryEntry(document);

		// WHEN
		boolean result = memorandum.isParent(thing);

		// THEN
		assertThat(result).isTrue();
	}

	@Test
	public void onInvalidParentShouldReturnFalse() throws Exception {
		// GIVEN
		RegistryEntry<Document> document = new RegistryEntry<>(newRepositoryInformation(Document.class, "documents"), null);
		RegistryEntry<Task> task = new RegistryEntry<>(newRepositoryInformation(Task.class, "tasks"), null);

		// WHEN
		boolean result = document.isParent(task);

		// THEN
		assertThat(result).isFalse();
	}

	@Test
	public void equalsContract() throws NoSuchFieldException {
		ModuleRegistry moduleRegistry = new ModuleRegistry();
		RegistryEntry blue = new RegistryEntry(newRepositoryInformation(String.class, "strings"), null);
		RegistryEntry red = new RegistryEntry(newRepositoryInformation(Long.class, "longs"), null);
		EqualsVerifier.forClass(RegistryEntry.class).withPrefabValues(RegistryEntry.class, blue, red)
				.withPrefabValues(ResourceInformation.class, new ResourceInformation(String.class, null, null),
						new ResourceInformation(Long.class, null, null))
				.withPrefabValues(Field.class, String.class.getDeclaredField("value"), String.class.getDeclaredField("hash"))
				.withPrefabValues(ModuleRegistry.class, new ModuleRegistry(), new ModuleRegistry())
				.suppress(Warning.NONFINAL_FIELDS).suppress(Warning.STRICT_INHERITANCE).verify();
	}
}
