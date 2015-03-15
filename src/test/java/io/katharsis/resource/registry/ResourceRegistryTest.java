package io.katharsis.resource.registry;

import io.katharsis.resource.ResourceNotFoundException;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.models.UnAnnotatedTask;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ResourceRegistryTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void onExistingTypeShouldReturnEntry() {
        // GIVEN
        ResourceRegistry sut = new ResourceRegistry();
        sut.addEntry(Task.class, new RegistryEntry<>(null));

        // WHEN
        RegistryEntry tasksEntry = sut.getEntry("tasks");

        // THEN
        Assert.assertNotNull(tasksEntry);
    }

    @Test
    public void onNonExistingTypeShouldThrowException() {
        // GIVEN
        ResourceRegistry sut = new ResourceRegistry();

        // THEN
        expectedException.expect(ResourceNotFoundException.class);

        // WHEN
        sut.getEntry("nonExistingType");
    }

    @Test
    public void onUnAnnotatedTypeShouldThrowException() {
        // GIVEN
        ResourceRegistry sut = new ResourceRegistry();
        sut.addEntry(UnAnnotatedTask.class, new RegistryEntry<>(null));

        // THEN
        expectedException.expect(RuntimeException.class);

        // WHEN
        sut.getEntry("tasks");
    }
}
