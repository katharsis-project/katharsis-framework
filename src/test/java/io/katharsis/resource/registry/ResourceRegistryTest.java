package io.katharsis.resource.registry;

import io.katharsis.resource.exception.ResourceNotFoundException;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.models.UnAnnotatedTask;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ResourceRegistryTest {

    public static final String TEST_MODELS_URL = "https://service.local";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void onExistingTypeShouldReturnEntry() {
        // GIVEN
        ResourceRegistry sut = new ResourceRegistry(TEST_MODELS_URL);
        sut.addEntry(Task.class, new RegistryEntry<>(null, null));

        // WHEN
        RegistryEntry tasksEntry = sut.getEntry("tasks");

        // THEN
        Assert.assertNotNull(tasksEntry);
    }

    @Test
    public void onExistingClassShouldReturnEntry() {
        // GIVEN
        ResourceRegistry sut = new ResourceRegistry(TEST_MODELS_URL);
        sut.addEntry(Task.class, new RegistryEntry<>(null, null));

        // WHEN
        RegistryEntry tasksEntry = sut.getEntry(Task.class);

        // THEN
        Assert.assertNotNull(tasksEntry);
    }

    @Test
    public void onExistingTypeShouldReturnUrl() {
        // GIVEN
        ResourceRegistry sut = new ResourceRegistry(TEST_MODELS_URL);
        sut.addEntry(Task.class, new RegistryEntry<>(null, null));

        // WHEN
        String resourceUrl = sut.getResourceUrl(Task.class);

        // THEN
        Assert.assertEquals(TEST_MODELS_URL + "/tasks", resourceUrl);
    }

    @Test
    public void onNonExistingTypeShouldThrowException() {
        // GIVEN
        ResourceRegistry sut = new ResourceRegistry(TEST_MODELS_URL);

        // THEN
        expectedException.expect(ResourceNotFoundException.class);

        // WHEN
        sut.getEntry("nonExistingType");
    }

    @Test
    public void onNonExistingClassShouldThrowException() {
        // GIVEN
        ResourceRegistry sut = new ResourceRegistry(TEST_MODELS_URL);

        // THEN
        expectedException.expect(ResourceNotFoundException.class);

        // WHEN
        sut.getEntry(Long.class);
    }

    @Test
    public void onUnAnnotatedTypeShouldThrowException() {
        // GIVEN
        ResourceRegistry sut = new ResourceRegistry(TEST_MODELS_URL);
        sut.addEntry(UnAnnotatedTask.class, new RegistryEntry<>(null, null));

        // THEN
        expectedException.expect(RuntimeException.class);

        // WHEN
        sut.getEntry("tasks");
    }
}
