package io.katharsis.resource.registry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import io.katharsis.core.internal.repository.information.ResourceRepositoryInformationImpl;
import io.katharsis.errorhandling.exception.ResourceNotFoundInitializationException;
import io.katharsis.module.ModuleRegistry;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.utils.Optional;

public class ResourceRegistryTest {

	public static final String TEST_MODELS_URL = "https://service.local";

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private ResourceRegistry resourceRegistry;

	@Before
	public void resetResourceRegistry() {
		resourceRegistry = new ResourceRegistry(new ModuleRegistry(), new ConstantServiceUrlProvider(TEST_MODELS_URL));
	}

	@Test
	public void onExistingTypeShouldReturnEntry() {
		resourceRegistry.addEntry(Task.class, newRegistryEntry(Task.class, "tasks"));
		RegistryEntry<Task> tasksEntry = resourceRegistry.getEntry("tasks");
		assertThat(tasksEntry).isNotNull();
	}

	private <T> RegistryEntry<T> newRegistryEntry(Class<T> repositoryClass, String path) {
		return new RegistryEntry<>(
				new ResourceRepositoryInformationImpl(null, path, new ResourceInformation(Task.class, path, null)),
				null, null);
	}

	@Test
	public void testGetSeriveUrlProvider() {
		assertThat(resourceRegistry.getServiceUrlProvider().getUrl()).isEqualTo(TEST_MODELS_URL);
	}

	@Test
	public void testGetServiceUrl() {
		assertThat(resourceRegistry.getServiceUrl()).isEqualTo(TEST_MODELS_URL);
	}

	@Test
	public void onExistingClassShouldReturnEntry() {
		resourceRegistry.addEntry(Task.class, newRegistryEntry(Task.class, "tasks"));
		RegistryEntry<Task> tasksEntry = resourceRegistry.getEntry(Task.class);
		assertThat(tasksEntry).isNotNull();
	}

	@Test
	public void onExistingTypeShouldReturnUrl() {
		resourceRegistry.addEntry(Task.class, newRegistryEntry(Task.class, "tasks"));
		String resourceUrl = resourceRegistry.getResourceUrl(Task.class);
		assertThat(resourceUrl).isEqualTo(TEST_MODELS_URL + "/tasks");
	}

	@Test
	public void onNonExistingTypeShouldReturnNull() {
		RegistryEntry entry = resourceRegistry.getEntry("nonExistingType");
		assertThat(entry).isNull();
	}

	@Test
	public void onNonExistingClassShouldThrowException() {
		expectedException.expect(ResourceNotFoundInitializationException.class);
		resourceRegistry.getEntry(Long.class);
	}

	@Test
	public void onNonExistingClassShouldReturnNull() {
		String result = resourceRegistry.getResourceType(Long.class);
		assertThat(result).isNull();
	}

	@Test
	public void onResourceClassReturnCorrectClass() {
		resourceRegistry.addEntry(Task.class, newRegistryEntry(Task.class, "tasks"));

		// WHEN
		Class<?> clazz = resourceRegistry.getResourceClass(Task$Proxy.class).get();

		// THEN
		assertThat(clazz).isNotNull();
		assertThat(clazz).hasAnnotation(JsonApiResource.class);
		assertThat(clazz).isEqualTo(Task.class);
	}

	@Test
	public void onResourceClassReturnCorrectParentInstanceClass() {
		resourceRegistry.addEntry(Task.class, newRegistryEntry(Task.class, "tasks"));
		Task$Proxy resource = new Task$Proxy();

		// WHEN
		Class<?> clazz = resourceRegistry.getResourceClass(resource).get();

		// THEN
		assertThat(clazz).isEqualTo(Task.class);
	}

	@Test
	public void onResourceClassReturnCorrectInstanceClass() {
		resourceRegistry.addEntry(Task.class, newRegistryEntry(Task.class, "tasks"));
		Task resource = new Task();

		// WHEN
		Class<?> clazz = resourceRegistry.getResourceClass(resource).get();

		// THEN
		assertThat(clazz).isEqualTo(Task.class);
	}

	@Test
	public void onResourceClassReturnNoInstanceClass() {
		resourceRegistry.addEntry(Task.class, newRegistryEntry(Task.class, "tasks"));
		Object resource = new Object();

		// WHEN
		Optional<Class<?>> clazz = resourceRegistry.getResourceClass(resource);

		// THEN
		assertThat(clazz.isPresent()).isFalse();
	}

	@Test
	public void onResourceGetEntryWithBackUp() {
		String taskType = Task.class.getAnnotation(JsonApiResource.class).type();
		resourceRegistry.addEntry(Task.class, newRegistryEntry(Task.class, taskType));

		// WHEN
		RegistryEntry<Task> registryEntry = resourceRegistry.getEntry(taskType, Task.class);

		// THEN
		assertNotNull(registryEntry);
		assertNotNull(registryEntry.getResourceInformation().getResourceType(), taskType);

		// WHEN
		registryEntry = resourceRegistry.getEntry("N/A", Task.class);

		// THEN
		assertNotNull(registryEntry);
		assertNotNull(registryEntry.getResourceInformation().getResourceType(), taskType);
	}

	public static class Task$Proxy extends Task {
	}

}
